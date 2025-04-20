package ua.com.pimenov.latte.runs

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.util.concurrent.ExecutionException

class RunConfiguration(
    project: Project,
    factory: ConfigurationFactory?,
    name: String?
) : RunConfigurationBase<RunConfigurationOptions?>(project, factory, name) {

    override fun getOptions(): RunConfigurationOptions {
        return super.getOptions() as RunConfigurationOptions
    }

    var configFile: String?
        get() = options.configFile
        set(value) {
            options.configFile = value ?: ""
        }

    var nodeInterpreter: String?
        get() = options.nodeInterpreter
        set(value) {
            options.nodeInterpreter = value ?: ""
        }

    var nodeOptions: String?
        get() = options.nodeOptions
        set(value) {
            options.nodeOptions = value ?: ""
        }

    var lattePath: String?
        get() = options.lattePath
        set(value) {
            options.lattePath = value ?: ""
        }

    var workingDirectory: String?
        get() = options.workingDirectory ?: project.basePath ?: ""
        set(value) {
            options.workingDirectory = value ?: ""
        }

    var latteOptions: String?
        get() = options.latteOptions
        set(value) {
            options.latteOptions = value ?: ""
        }

    var envVariables: MutableMap<String, String>
        get() = options.envVariables
        set(value) {
            options.envVariables = value
        }

    var testScope: String?
        get() = options.testScope
        set(value) {
            options.testScope = value ?: ""
        }

    var testsDirectory: String?
        get() = options.testsDirectory
        set(value) {
            options.testsDirectory = value ?: ""
        }

    var testsFile: String?
        get() = options.testsFile
        set(value) {
            options.testsFile = value ?: ""
        }

    var suiteFile: String?
        get() = options.suiteFile
        set(value) {
            options.suiteFile = value ?: ""
        }

    var suiteName: String?
        get() = options.suiteName
        set(value) {
            options.suiteName = value ?: ""
        }

    var testFile: String?
        get() = options.testFile
        set(value) {
            options.testFile = value ?: ""
        }

    var testName: String?
        get() = options.testName
        set(value) {
            options.testName = value ?: ""
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RunSettingsEditor(project) as SettingsEditor<out RunConfiguration>
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return object : CommandLineState(environment) {
            @Throws(ExecutionException::class)
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine()
                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}