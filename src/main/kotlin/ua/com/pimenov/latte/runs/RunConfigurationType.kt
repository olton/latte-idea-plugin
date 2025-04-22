package ua.com.pimenov.latte.runs

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeBase
import ua.com.pimenov.latte.utils.LatteIcons

class RunConfigurationType: ConfigurationTypeBase(
    ID,
    "Latte Test Runner",
    "Run tests with Latte",
    LatteIcons.LatteIcon
) {
    init {
        addFactory(createFactory())
    }

    private fun createFactory(): RunConfigurationFactory {
        return RunConfigurationFactory(this)
    }

    companion object {
        const val ID: String = "LatteRunConfiguration"

        // Метод getInstance для отримання зареєстрованого розширення
        fun getInstance(): RunConfigurationType {
            return ConfigurationType.CONFIGURATION_TYPE_EP.extensionList
                .find { it.id == ID } as RunConfigurationType
        }
    }
}

