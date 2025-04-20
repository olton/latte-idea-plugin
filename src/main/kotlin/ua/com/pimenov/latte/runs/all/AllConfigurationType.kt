package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configurations.ConfigurationTypeBase
import ua.com.pimenov.latte.utils.LatteIcons

class AllConfigurationType: ConfigurationTypeBase(
    ID,
    "Latte",
    "",
    LatteIcons.LatteIcon
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

