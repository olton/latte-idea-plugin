package ua.com.pimenov.latte.runs

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import ua.com.pimenov.latte.data.TEST_FILE_EXTENSIONS

class JsTestRunConfigurationProducer : LazyRunConfigurationProducer<LatteRunConfiguration>() {

    private fun isTestFile(file: VirtualFile): Boolean {
        val fileName = file.name
        return TEST_FILE_EXTENSIONS.any { fileName.endsWith(it) }
    }

    override fun getConfigurationFactory(): ConfigurationFactory {
        return LatteRunConfigurationType.getInstance().configurationFactories.first()
    }

    override fun setupConfigurationFromContext(
        configuration: LatteRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = sourceElement.get() ?: return false
        val file = element.containingFile?.virtualFile ?: return false

        if (!isTestFile(file)) {
            // Не створювати конфігурацію для не-тестових файлів
            return false
        }

        // Логіка налаштування конфігурації запуску для конкретного тесту
        // Тут потрібно визначити ім'я тесту і встановити правильні параметри запуску

        return true
    }

    override fun isConfigurationFromContext(
        configuration: LatteRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.psiLocation ?: return false
        val file = location.containingFile?.virtualFile ?: return false

        if (!isTestFile(file)) {
            // Не вважати цю конфігурацію відповідною для не-тестових файлів
            return false
        }

        // Логіка перевірки чи підходить існуюча конфігурація для поточного контексту
        return false
    }
}
