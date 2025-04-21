package ua.com.pimenov.latte.runs

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull

class RunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    @NotNull
    override fun getId(): String = RunConfigurationType.ID

    @NotNull
    override fun createTemplateConfiguration(@NotNull project: Project): RunConfiguration {
        return RunConfiguration(project, this, "Latte Testing")
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return RunConfigurationOptions::class.java
    }
}