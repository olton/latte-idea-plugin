package ua.com.pimenov.latte.actions

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ua.com.pimenov.latte.Latte
import ua.com.pimenov.latte.markers.JsTestRunLineMarkerContributor
import ua.com.pimenov.latte.runs.RunConfigurationType
import ua.com.pimenov.latte.runs.ScopeType
import javax.swing.Icon

class RunTestFileAction : AnAction() {
    companion object {
        private val TEST_FILE_EXTENSIONS = setOf("test.js", "test.jsx", "test.ts", "test.tsx", "spec.js", "spec.jsx", "spec.ts", "spec.tsx")
    }

    // Встановлюємо іконку та текст для дії
    init {
        templatePresentation.text = Latte.message("latte.action.run.test.file")
        templatePresentation.description = Latte.message("latte.action.run.test.file.description")
        templatePresentation.icon = JsTestRunLineMarkerContributor.RUN_FILE_ICON
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        // Запускаємо тестовий файл
        executeTestFile(project, file)
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // Показуємо дію тільки для тестових файлів
        presentation.isEnabledAndVisible = project != null && file != null && isTestFile(file)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun isTestFile(file: VirtualFile): Boolean {
        val fileName = file.name
        return TEST_FILE_EXTENSIONS.any { fileName.endsWith(it) }
    }

    private fun executeTestFile(project: Project, file: VirtualFile) {
        val runName = "File: ${file.name}"

        // Отримуємо менеджер запуску
        val runManager = RunManager.getInstance(project)

        // Створюємо нову конфігурацію запуску
        val configurationType = RunConfigurationType.getInstance()
        val factory = configurationType.configurationFactories.firstOrNull() ?: return
        val settings = runManager.createConfiguration(runName, factory)

        // Перевіряємо чи це TypeScript файл для додавання NODE_OPTIONS
        val isTypeScriptFile = file.path.endsWith(".ts") || file.path.endsWith(".tsx")

        // Налаштовуємо параметри конфігурації
        (settings.configuration as? ua.com.pimenov.latte.runs.RunConfiguration)?.let { config ->
            config.testScope = ScopeType.FILE.id
            config.testsFile = file.path
            config.latteOptions = "--dom"

            // Додаємо NODE_OPTIONS=--import tsx для TypeScript файлів
            if (isTypeScriptFile) {
                // Отримуємо поточні змінні оточення або створюємо нову мапу
                val envVars = config.envVariables

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