package ua.com.pimenov.latte.runs

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class JsTestRunConfigurationProducer : RunConfigurationProducer<RunConfiguration>(RunConfigurationType()) {

    override fun setupConfigurationFromContext(
        configuration: RunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = sourceElement.get() ?: return false

        // Логіка налаштування конфігурації запуску для конкретного тесту
        // Тут потрібно визначити ім'я тесту і встановити правильні параметри запуску

        return true
    }

    override fun isConfigurationFromContext(
        configuration: RunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        // Логіка перевірки чи підходить існуюча конфігурація для поточного контексту
        return false
    }
}