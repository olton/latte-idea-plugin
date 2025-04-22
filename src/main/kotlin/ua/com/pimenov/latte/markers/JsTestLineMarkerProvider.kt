package ua.com.pimenov.latte.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.execution.Location
import com.intellij.execution.ProgramRunnerUtil
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

private enum class TestFunctionType {
    SUITE, TEST
}

class JsTestLineMarkerProvider : LineMarkerProvider {

    companion object {
        private val RUN_TEST_ICON = AllIcons.RunConfigurations.TestState.Run
        private val RUN_SUITE_ICON = AllIcons.RunConfigurations.TestState.Run_run
        private val TEST_FILE_EXTENSIONS = setOf("test.js", "test.jsx", "test.ts", "test.tsx")
        private val TEST_FUNCTION_NAMES = setOf("describe", "it", "test")
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        // Перевіряємо чи файл має відповідне розширення
        if (!isTestFile(element.containingFile)) {
            return null
        }

        // Перевіряємо чи елемент є викликом тестової функції
        if (!isTestFunction(element)) {
            return null
        }

        // Створюємо і повертаємо LineMarkerInfo
        return createLineMarkerInfo(element)
    }

    private fun isTestFile(file: PsiFile): Boolean {
        val fileName = file.name
        return TEST_FILE_EXTENSIONS.any { fileName.endsWith(it) }
    }

    private fun isTestFunction(element: PsiElement): Boolean {
        // Якщо елемент не є ідентифікатором - виходимо
        if (element !is JSReferenceExpression) {
            return false
        }

        // Отримуємо текст ідентифікатора
        val identifierText = element.text

        // Перевіряємо чи це один з тестових методів
        if (!TEST_FUNCTION_NAMES.contains(identifierText)) {
            return false
        }

        // Перевіряємо чи це виклик функції
        val parent = element.parent
        return parent is JSCallExpression
    }

    private fun createLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement> {
        // Визначаємо тип тестової функції
        val functionType = if (element is JSReferenceExpression) {
            when (element.text) {
                "describe" -> TestFunctionType.SUITE
                "it", "test" -> TestFunctionType.TEST
                else -> TestFunctionType.TEST
            }
        } else {
            TestFunctionType.TEST
        }

        // Обираємо іконку та повідомлення відповідно до типу функції
        val icon = when (functionType) {
            TestFunctionType.SUITE -> RUN_SUITE_ICON
            TestFunctionType.TEST -> RUN_TEST_ICON
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
        val testName = getTestName(element)

        // Створюємо простий DataContext на основі PsiElement
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.PSI_ELEMENT, element)
            .add(CommonDataKeys.VIRTUAL_FILE, file)
            .build()

        // Створюємо контекст запуску
        val context = ConfigurationContext.createEmptyContextForLocation(element as Location<*>)

        // Спроба знайти існуючу конфігурацію або створити нову
        val settings = context.getConfiguration()?.configuration?.let {
            RunManager.getInstance(project).findSettings(it)
        } ?: createRunConfiguration(element, testName)

        // Якщо не вдалося створити конфігурацію, виходимо
        if (settings == null) {
            return
        }

        // Запускаємо конфігурацію
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(settings, executor)
    }

    private fun createRunConfiguration(element: PsiElement, testName: String): RunnerAndConfigurationSettings? {
        val project = element.project
        val file = element.containingFile.virtualFile

        // Отримуємо менеджер запуску
        val runManager = RunManager.getInstance(project)

        // Створюємо нову конфігурацію запуску
        val configurationType = RunConfigurationType.getInstance()
        val factory = configurationType.configurationFactories.firstOrNull() ?: return null
        val settings = runManager.createConfiguration(testName, factory)

        // Налаштовуємо параметри конфігурації
        val configuration = settings.configuration as? ua.com.pimenov.latte.runs.RunConfiguration
        if (configuration != null) {
            configuration.testFile = file.path
            configuration.testName = testName

            // Зберігаємо конфігурацію
            runManager.addConfiguration(settings)
            return settings
        }

        return null
    }

    private fun getTestName(element: PsiElement): String {
        var testName = "Unknown Test"

        // Якщо елемент є JSReferenceExpression і його батько - JSCallExpression
        if (element is JSReferenceExpression && element.parent is JSCallExpression) {
            val callExpression = element.parent as JSCallExpression

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
            testName = when (functionName) {
                "describe" -> "Suite: $testName"
                "it", "test" -> "Test: $testName"
                else -> testName
            }

            // Для вкладених тестів (test внутрі describe) намагаємося зібрати повний шлях
            if (functionName == "it" || functionName == "test") {
                val parentDescribe = findParentDescribe(callExpression)
                if (parentDescribe != null) {
                    val describeRef = PsiTreeUtil.findChildOfType(parentDescribe, JSReferenceExpression::class.java)
                    if (describeRef != null) {
                        val describeName = getTestName(describeRef)
                        testName = "$describeName > $testName"
                    }
                }
            }
        }

        return testName
    }

    private fun findParentDescribe(element: PsiElement): JSCallExpression? {
        var parent = element.parent
        while (parent != null) {
            if (parent is JSCallExpression) {
                val reference = PsiTreeUtil.findChildOfType(parent, JSReferenceExpression::class.java)
                if (reference != null && reference.text == "describe") {
                    return parent
                }
            }
            parent = parent.parent
        }
        return null
    }
}