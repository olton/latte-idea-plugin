package ua.com.pimenov.latte.markers

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import ua.com.pimenov.latte.Latte
import ua.com.pimenov.latte.runs.RunConfigurationType
import ua.com.pimenov.latte.runs.ScopeType

/**
 * Лінійний маркер для запуску JavaScript тестів через іконки в gutter-і редактора.
 * Використовує новий RunLineMarkerContributor API для уникнення дублювання маркерів.
 */
class JsTestRunLineMarkerContributor : RunLineMarkerContributor() {

    companion object {
        val TEST_FILE_EXTENSIONS = setOf("test.js", "test.jsx", "test.ts", "test.tsx",
            "spec.js", "spec.jsx", "spec.ts", "spec.tsx")
        val TEST_FUNCTION_NAMES = setOf("describe", "suite", "it", "test")

        // Іконки для різних типів тестів
        val RUN_TEST_ICON = AllIcons.RunConfigurations.TestState.Run
        val RUN_SUITE_ICON = AllIcons.RunConfigurations.TestState.Run_run
        val RUN_FILE_ICON = AllIcons.RunConfigurations.TestState.Run
    }

    /**
     * Головний метод, що викликається для кожного елемента PSI-дерева.
     * Повертає Info з іконкою та діями, якщо елемент є підходящим для тестового запуску.
     */
    override fun getInfo(element: PsiElement): Info? {
        // Перевіряємо, чи файл є тестовим
        if (!isTestFile(element.containingFile)) {
            return null
        }

        // Важливо! Працюємо тільки з leaf елементами (точковими елементами)
        // Це запобігає створенню дублікатів маркерів
        if (element.firstChild != null) {
            return null
        }

        // Знаходимо батьківський JSReferenceExpression
        val refExpression = findParentReferenceExpression(element)
        if (refExpression == null || !TEST_FUNCTION_NAMES.contains(refExpression.text)) {
            return null
        }

        // Перевіряємо, чи це частина виклику функції
        val callExpression = refExpression.parent as? JSCallExpression ?: return null

        // Перевіряємо, чи це перший елемент в ідентифікаторі функції,
        // щоб не дублювати маркери для кожного дочірнього елемента
        if (element != refExpression.firstChild) {
            return null
        }

        // Визначаємо тип функції та відповідну іконку
        val functionType = getFunctionType(refExpression)
        val icon = when (functionType) {
            "describe", "suite" -> RUN_SUITE_ICON
            "it", "test" -> RUN_TEST_ICON
            else -> RUN_TEST_ICON
        }

        // Отримуємо ім'я тесту
        val testName = getTestName(callExpression)

        // Створюємо дію для запуску тесту
        val action = createRunAction(element, testName, functionType)

        // Створюємо текст підказки
        val tooltipProvider = { _: PsiElement ->
            when (functionType) {
                "describe", "suite" -> Latte.message("latte.line.marker.tooltip.run.suite")
                "it", "test" -> Latte.message("latte.line.marker.tooltip.run.test")
                else -> "Run Test"
            }
        }

        // Використовуємо конструктор, який приймає іконку, дії та функцію підказки
        return Info(icon, arrayOf(action), tooltipProvider)
    }

    /**
     * Знаходить батьківський JSReferenceExpression для елемента
     */
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

    /**
     * Перевіряє чи файл є тестовим за розширенням
     */
    private fun isTestFile(file: PsiFile): Boolean {
        val fileName = file.name
        return TEST_FILE_EXTENSIONS.any { fileName.endsWith(it) }
    }

    /**
     * Визначає тип функції (suite чи test)
     */
    private fun getFunctionType(element: PsiElement): String {
        return element.text
    }

    /**
     * Отримує ім'я тесту з аргументів виклику функції
     */
    private fun getTestName(callExpression: JSCallExpression): String {
        val arguments = callExpression.arguments
        if (arguments.isNotEmpty() && arguments[0] is JSLiteralExpression) {
            val literalExpression = arguments[0] as JSLiteralExpression
            if (literalExpression.isQuotedLiteral) {
                return literalExpression.stringValue ?: "Unknown Test"
            }
        }
        return "Unknown Test"
    }

    /**
     * Створює дію для запуску тесту
     */
    private fun createRunAction(element: PsiElement, testName: String, functionType: String): AnAction {
        // Текст для дії
        val actionText = when (functionType) {
            "describe", "suite" -> "Run Suite '${testName}'"
            "it", "test" -> "Run Test '${testName}'"
            else -> "Run '${testName}'"
        }

        // Створюємо дію з конкретним текстом
        return object : AnAction(actionText) {
            override fun actionPerformed(e: AnActionEvent) {
                executeTest(element, testName, functionType)
            }

            override fun getActionUpdateThread(): ActionUpdateThread {
                return ActionUpdateThread.BGT
            }

            // Встановлюємо іконку для дії
            init {
                val icon = when (functionType) {
                    "describe", "suite" -> RUN_SUITE_ICON
                    "it", "test" -> RUN_TEST_ICON
                    else -> RUN_TEST_ICON
                }
                templatePresentation.icon = icon
            }
        }
    }

    /**
     * Запускає тест через конфігурацію запуску
     */
    private fun executeTest(element: PsiElement, testName: String, functionType: String) {
        val project = element.project
        val file = element.containingFile.virtualFile.path

        // Формуємо ім'я запуску
        val runName = when (functionType) {
            "describe", "suite" -> "Suite: $testName"
            "it", "test" -> "Test: $testName"
            else -> testName
        }

        // Отримуємо менеджер запуску
        val runManager = RunManager.getInstance(project)

        // Створюємо нову конфігурацію запуску
        val configurationType = RunConfigurationType.getInstance()
        val factory = configurationType.configurationFactories.firstOrNull() ?: return
        val settings = runManager.createConfiguration(runName, factory)

        // Перевіряємо чи це TypeScript файл для додавання NODE_OPTIONS
        val isTypeScriptFile = file.endsWith(".ts") || file.endsWith(".tsx") || file.endsWith(".jsx")

        // Налаштовуємо параметри конфігурації
        (settings.configuration as? ua.com.pimenov.latte.runs.RunConfiguration)?.let { config ->
            when (functionType) {
                "describe", "suite" -> {
                    config.testScope = ScopeType.SUITE.id
                    config.suiteFile = file
                    config.suiteName = testName
                }
                "it", "test" -> {
                    config.testScope = ScopeType.TEST.id
                    config.testFile = file
                    config.testName = testName
                }
                else -> {
                    config.testScope = ScopeType.ALL.id
                }
            }

            config.latteOptions = "--dom"
            
            // Додаємо NODE_OPTIONS=--import tsx для TypeScript файлів
            if (isTypeScriptFile) {
                // Отримуємо поточні змінні оточення або створюємо нову мапу
                val envVars = config.envVariables ?: mutableMapOf()

                // Додаємо або оновлюємо NODE_OPTIONS
                val currentNodeOptions = envVars["NODE_OPTIONS"] ?: ""
                val updatedNodeOptions = if (currentNodeOptions.contains("--import tsx")) {
                    currentNodeOptions
                } else {
                    if (currentNodeOptions.isEmpty()) "--import tsx" else "$currentNodeOptions --import tsx"
                }

                envVars["NODE_OPTIONS"] = updatedNodeOptions
                config.envVariables = envVars
            }

            // Зберігаємо конфігурацію
            runManager.addConfiguration(settings)

            // Запускаємо конфігурацію
            val executor = DefaultRunExecutor.getRunExecutorInstance()
            ProgramRunnerUtil.executeConfiguration(settings, executor)
        }
    }
}