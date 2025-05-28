package ua.com.pimenov.latte.runs

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeBase
import ua.com.pimenov.latte.utils.LatteIcons

class LatteRunConfigurationType: ConfigurationTypeBase(
    ID,
    "Latte Test Runner",
    "Run tests with Latte",
    LatteIcons.LatteIcon
) {
    init {
        addFactory(createFactory())
    }

    private fun createFactory(): LatteRunConfigurationFactory {
        return LatteRunConfigurationFactory(this)
    }

    companion object {
        const val ID: String = "LatteRunConfiguration"

        // Метод getInstance для отримання зареєстрованого розширення
        fun getInstance(): LatteRunConfigurationType {
            return ConfigurationType.CONFIGURATION_TYPE_EP.extensionList
                .find { it.id == ID } as LatteRunConfigurationType
        }
    }
}

