package ua.com.pimenov.latte.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Function
import ua.com.pimenov.latte.runs.RunConfigurationType
import ua.com.pimenov.latte.Latte
import ua.com.pimenov.latte.runs.ScopeType

private enum class TestFunctionType {
    SUITE, TEST
}

class JsTestLineMarkerProvider : LineMarkerProvider {

    companion object {
        private val RUN_TEST_ICON = AllIcons.RunConfigurations.TestState.Run
        private val RUN_SUITE_ICON = AllIcons.RunConfigurations.TestState.Run_run
        private val TEST_FILE_EXTENSIONS = setOf("test.js", "test.jsx", "test.ts", "test.tsx", "spec.js", "spec.jsx", "spec.ts", "spec.tsx")
        private val TEST_FUNCTION_NAMES = setOf("describe", "suite", "it", "test")
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        // Перевіряємо чи файл має відповідне розширення
        if (!isTestFile(element.containingFile)) {
            return null
        }

        // Працюємо лише з точковими елементами (leaf elements)
        if (element.firstChild != null) {
            return null
        }

        // Перевіряємо чи елемент є частиною виклику тестової функції
        val refExpression = findParentReferenceExpression(element)
        if (refExpression == null || !isTestFunction(refExpression)) {
            return null
        }

        // Створюємо і повертаємо LineMarkerInfo для точкового елемента
        return createLineMarkerInfo(element, refExpression)
    }

    private fun findParentReferenceExpression(element: PsiElement): JSReferenceExpression? {
        var current = element
        // Якщо елемент є частиною ідентифікатора, знаходимо батьківський JSReferenceExpression
        while (current.parent != null) {
            if (current.parent is JSReferenceExpression) {
                return current.parent as JSReferenceExpression
            }
            current = current.parent
        }
        return null
    }

    private fun isTestFile(file: PsiFile): Boolean {
        val fileName = file.name
        return TEST_FILE_EXTENSIONS.any { fileName.endsWith(it) }
    }

    private fun isTestFunction(element: JSReferenceExpression): Boolean {
        // Отримуємо текст ідентифікатора
        val identifierText = element.text

        // Перевіряємо, чи це один з тестових методів
        if (!TEST_FUNCTION_NAMES.contains(identifierText)) {
            return false
        }

        // Перевіряємо, чи це виклик функції
        val parent = element.parent
        return parent is JSCallExpression
    }

    private fun getFunctionType(element: PsiElement): TestFunctionType? {
        return when (element.text) {
            "describe", "suite" -> TestFunctionType.SUITE
            "it", "test" -> TestFunctionType.TEST
            else -> TestFunctionType.TEST
        }
    }

    private fun createLineMarkerInfo(element: PsiElement, refExpression: JSReferenceExpression): LineMarkerInfo<PsiElement> {
        // Визначаємо тип тестової функції
        val functionType = getFunctionType(refExpression)

        // Обираємо іконку та повідомлення відповідно до типу функції
        val icon = when (functionType) {
            TestFunctionType.SUITE -> RUN_SUITE_ICON
            TestFunctionType.TEST -> RUN_TEST_ICON
            null -> TODO()
        }

        val tooltipText = when (functionType) {
            TestFunctionType.SUITE -> Latte.message("latte.line.marker.tooltip.run.suite")
            TestFunctionType.TEST -> Latte.message("latte.line.marker.tooltip.run.test")
        }

        return LineMarkerInfo(
            element,
            element.textRange,
            icon,
            Function { tooltipText },
            { e, elt -> executeTest(elt) },
            GutterIconRenderer.Alignment.CENTER,
            { tooltipText }
        )
    }

    private fun executeTest(element: PsiElement) {
        val project = element.project
        val file = element.containingFile.virtualFile

        // Отримуємо ім'я тесту для запуску
        val (testName, runName) = getTestName(element)

        val location = PsiLocation(project, element)

        // Створюємо контекст запуску
        val context = ConfigurationContext.createEmptyContextForLocation(location)

        // Спроба знайти існуючу конфігурацію або створити нову
        val settings = context.getConfiguration()?.configuration?.let {
            RunManager.getInstance(project).findSettings(it)
        } ?: createRunConfiguration(element, testName, runName)

        // Якщо не вдалося створити конфігурацію, виходимо
        if (settings == null) {
            return
        }

        // Запускаємо конфігурацію
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(settings, executor)
    }

    private fun createRunConfiguration(element: PsiElement, testName: String, runName: String): RunnerAndConfigurationSettings? {
        val project = element.project
        val file = element.containingFile.virtualFile

        // Отримуємо менеджер запуску
        val runManager = RunManager.getInstance(project)

        // Створюємо нову конфігурацію запуску
        val configurationType = RunConfigurationType.getInstance()
        val factory = configurationType.configurationFactories.firstOrNull() ?: return null
        val settings = runManager.createConfiguration(runName, factory)

        val refExpression = if (element is JSReferenceExpression) element else findParentReferenceExpression(element)
        val functionType = refExpression?.let { getFunctionType(it) } ?: TestFunctionType.TEST

        // Налаштовуємо параметри конфігурації, переконавшись що це правильний тип
        (settings.configuration as? ua.com.pimenov.latte.runs.RunConfiguration)?.let { config ->
            // Встановлюємо параметри конфігурації
            config.testScope = when (functionType) {
                TestFunctionType.SUITE -> ScopeType.SUITE.id
                TestFunctionType.TEST -> ScopeType.TEST.id
                else -> ScopeType.ALL.id
            }

            if (functionType == TestFunctionType.SUITE) {
                config.testScope = ScopeType.SUITE.id
                config.suiteFile = file.path
                config.suiteName = testName
            } else if (functionType == TestFunctionType.TEST) {
                config.testScope = ScopeType.TEST.id
                config.testFile = file.path
                config.testName = testName
            } else {
                config.testScope = ScopeType.ALL.id
                config.suiteFile = ""
                config.suiteName = ""
                config.testFile = ""
                config.testName = ""
            }

            // Зберігаємо конфігурацію
            runManager.addConfiguration(settings)
            return settings
        }

        // Якщо приведення типу не вдалося, повертаємо null
        return null
    }

    private fun getTestName(element: PsiElement): Pair<String, String> {
        var testName = "Unknown Test"
        var runName = "Unknown Run"

        val refExpression = if (element is JSReferenceExpression) element else findParentReferenceExpression(element)

        // Якщо елемент є JSReferenceExpression і його батько - JSCallExpression
        if (refExpression != null && refExpression.parent is JSCallExpression) {
            val callExpression = refExpression.parent as JSCallExpression

            // Отримуємо аргументи виклику функції
            val arguments = callExpression.arguments
            if (arguments.isNotEmpty() && arguments[0] is JSLiteralExpression) {
                val literalExpression = arguments[0] as JSLiteralExpression

                // Якщо перший аргумент є рядком, використовуємо його як ім'я тесту
                if (literalExpression.isQuotedLiteral) {
                    val stringValue = literalExpression.stringValue
                    if (stringValue != null) {
                        testName = stringValue
                    }
                }
            }

            // Додаємо префікс в залежності від типу тестової функції
            val functionName = element.text
            runName = when (functionName) {
                "describe", "suite" -> "Suite: $testName"
                "test" -> "Test: $testName"
                else -> testName
            }

            // Для вкладених тестів (test внутрі describe) намагаємося зібрати повний шлях
            if (functionName == "it") {
                val parentDescribe = findParentDescribe(callExpression)
                if (parentDescribe != null) {
                    val describeRef = PsiTreeUtil.findChildOfType(parentDescribe, JSReferenceExpression::class.java)
                    if (describeRef != null) {
                        val (describeName, _) = getTestName(describeRef)
                        runName = "$describeName > $testName"
                    }
                }
            }
        }

        return Pair(testName, runName)
    }

    private fun findParentDescribe(element: PsiElement): JSCallExpression? {
        var parent = element.parent
        while (parent != null) {
            if (parent is JSCallExpression) {
                val reference = PsiTreeUtil.findChildOfType(parent, JSReferenceExpression::class.java)
                if (reference != null && (reference.text == "describe" || reference.text == "suite")) {
                    return parent
                }
            }
            parent = parent.parent
        }
        return null
    }
}