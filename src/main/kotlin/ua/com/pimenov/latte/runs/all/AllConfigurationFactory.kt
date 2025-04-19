package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class AllConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    @NotNull
    override fun getId(): String = AllConfigurationType.ID

    @NotNull
    override fun createTemplateConfiguration(@NotNull project: Project): RunConfiguration {
        return AllRunConfiguration(project, this, "Latte")
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return AllConfigurationOptions::class.java
    }
}