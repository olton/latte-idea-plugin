package ua.com.pimenov.latte.runs.all

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class AllRunConfiguration(
    project: Project,
    factory: ConfigurationFactory?,
    name: String?
) : RunConfigurationBase<AllConfigurationOptions?>(project, factory, name) {

    override fun getOptions(): AllConfigurationOptions {
        return super.getOptions() as AllConfigurationOptions
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
        get() = options.workingDirectory
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

    var scopeDirectory: String?
        get() = options.scopeDirectory
        set(value) {
            options.scopeDirectory = value ?: ""
        }

    var scopeFile: String?
        get() = options.scopeFile
        set(value) {
            options.scopeFile = value ?: ""
        }

    var scopeSuite: String?
        get() = options.scopeSuite
        set(value) {
            options.scopeSuite = value ?: ""
        }

    var scopeTest: String?
        get() = options.scopeTest
        set(value) {
            options.scopeTest = value ?: ""
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return AllSettingsEditor(project) as SettingsEditor<out RunConfiguration>
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return object : CommandLineState(environment) {
//            @Throws(ExecutionException::class)
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine("")
                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}