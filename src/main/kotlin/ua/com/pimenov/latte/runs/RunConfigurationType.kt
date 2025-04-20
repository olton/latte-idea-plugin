package ua.com.pimenov.latte.runs

import com.intellij.execution.configurations.ConfigurationTypeBase
import ua.com.pimenov.latte.utils.LatteIcons

class RunConfigurationType: ConfigurationTypeBase(
    ID,
    "Latte",
    "Run all tests in project with latte",
    LatteIcons.LatteIcon
) {
    init {
        addFactory(createFactory())
    }

    private fun createFactory(): RunConfigurationFactory {
        return RunConfigurationFactory(this)
    }

    companion object {
        const val ID: String = "RunConfigurationAll"
    }
}

