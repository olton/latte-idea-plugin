package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons

class AllConfigurationType: ConfigurationTypeBase(
    ID, "Latte", "Run all tests",
    AllIcons.RunConfigurations.TestCustom
) {
    init {
        addFactory(createFactory())
    }

    private fun createFactory(): AllConfigurationFactory {
        return AllConfigurationFactory(this)
    }

    companion object {
        const val ID: String = "RunConfigurationAll"
    }
}

