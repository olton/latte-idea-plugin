package ua.com.pimenov.latte.runs

import com.beust.klaxon.Parser
import com.beust.klaxon.JsonObject
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
import ua.com.pimenov.latte.data.LatteConfig
import ua.com.pimenov.latte.utils.parseArgs
import java.util.concurrent.ExecutionException
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths

class RunConfiguration(
    project: Project,
    factory: ConfigurationFactory?,
    name: String?
) : RunConfigurationBase<RunConfigurationOptions?>(project, factory, name) {

    override fun getOptions(): RunConfigurationOptions {
        return super.getOptions() as RunConfigurationOptions
    }

    var configFile: String?
        get() = options.configFile ?: ""
        set(value) {
            options.configFile = value
        }

    var nodeInterpreter: String?
        get() = options.nodeInterpreter
        set(value) {
            options.nodeInterpreter = value ?: "node"
        }

    var nodeOptions: String?
        get() = options.nodeOptions  ?: ""
        set(value) {
            options.nodeOptions = value
        }

    var lattePath: String?
        get() = options.lattePath  ?: ""
        set(value) {
            options.lattePath = value
        }

    var workingDirectory: String?
        get() = options.workingDirectory ?: project.basePath ?: ""
        set(value) {
            options.workingDirectory = value
        }

    var latteOptions: String?
        get() = options.latteOptions ?: ""
        set(value) {
            options.latteOptions = value
        }

    var envVariables: MutableMap<String, String>
        get() = options.envVariables
        set(value) {
            options.envVariables = value
        }

    var testScope: String?
        get() = options.testScope  ?: "all"
        set(value) {
            options.testScope = value
        }

    var testsDirectory: String?
        get() = options.testsDirectory ?: ""
        set(value) {
            options.testsDirectory = value
        }

    var testsFile: String?
        get() = options.testsFile ?: ""
        set(value) {
            options.testsFile = value
        }

    var suiteFile: String?
        get() = options.suiteFile ?: ""
        set(value) {
            options.suiteFile = value
        }

    var suiteName: String?
        get() = options.suiteName ?: ""
        set(value) {
            options.suiteName = value
        }

    var testFile: String?
        get() = options.testFile ?: ""
        set(value) {
            options.testFile = value
        }

    var testName: String?
        get() = options.testName ?: ""
        set(value) {
            options.testName = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RunSettingsEditor(project) as SettingsEditor<out RunConfiguration>
    }

    private fun getLatteConfig(): Array<String>? {
        var configFileContent: String
        val latteConfig = LatteConfig()

        if (configFile != null && File(configFile!!).exists()) {
            configFileContent = File(configFile!!).readText()
            val sb: StringBuilder = StringBuilder(configFileContent)
            val json: JsonObject = Parser.default().parse(sb) as JsonObject
            json.entries.forEach {
                latteConfig[it.key] = it.value
            }
        }

        if (latteOptions != null && latteOptions!!.isNotEmpty()) {
            val parsedArgs = parseArgs(latteOptions!!)
            parsedArgs.forEach { (name, value) ->
                when (name) {
                    "--dom", "-d" -> { latteConfig.dom = true }
                    "--react", "-r" -> { latteConfig.react = true }
                    "--verbose", "-v" -> { latteConfig.verbose = true }
                    "--watch", "-w" -> { latteConfig.watch = true }
                    "--parallel", "-p" -> { latteConfig.parallel = true }
                    "--debug", "-g" -> { latteConfig.debug = true }
                    "--coverage", "-c" -> { latteConfig.coverage = true }
                    "--loader", "-l" -> { latteConfig.loader = true }
                    "--ts", "-t" -> { latteConfig.ts = true }
                    "--max-workers" -> { latteConfig.maxWorkers = value?.toInt()!! }
                    "--progress" -> { latteConfig.progress = value!! }
                    "--report-type" -> { latteConfig.reportType = value!! }
                    "--report-dir" -> { latteConfig.reportDir = value!! }
                    "--report-file" -> { latteConfig.reportFile = value!! }
                    "--include" -> { latteConfig.include = value!! }
                    "--exclude" -> { latteConfig.exclude = value!! }
                    "--test" -> { latteConfig.test = value!! }
                    "--suite" -> { latteConfig.suite = value!! }
                    "--clear-console" -> { latteConfig.clearConsole = true }
                    "--show-stack", "-s" -> { latteConfig.showStack = true }
                    "--dom-env" -> { latteConfig.domEnv = value!! }
                }
            }
        }

        var latteConfigString = emptyArray<String>()

        latteConfigString += "--idea"
        latteConfigString += "--skipConfigFile"

        if (latteConfig.dom) {latteConfigString += "--dom"}
        if (latteConfig.react) {latteConfigString += "--react"}
        if (latteConfig.verbose) {latteConfigString += "--verbose"}
        if (latteConfig.debug) {latteConfigString += "--debug"}
        if (latteConfig.loader) {latteConfigString += "--loader"}
        if (latteConfig.ts) {latteConfigString += "--ts"}
        if (latteConfig.clearConsole) {latteConfigString += "--clear-console"}
        if (latteConfig.watch) {latteConfigString += "--watch"}
        if (latteConfig.coverage) {latteConfigString += "--coverage"}
        if (latteConfig.parallel) {latteConfigString += "--parallel"}
        if (latteConfig.showStack) {latteConfigString += "--show-stack"}

        latteConfig.maxWorkers.let { if (latteConfig.parallel && it > 0) {latteConfigString += "--max-workers=${latteConfig.maxWorkers}"} }
        latteConfig.reportType.isEmpty().let { if (!it) {latteConfigString += "--report-type='${latteConfig.reportType}'"} }
        latteConfig.reportDir.isEmpty().let { if (!it) {latteConfigString += "--report-dir='${latteConfig.reportDir}'"} }
        latteConfig.reportFile.isEmpty().let { if (!it) {latteConfigString += "--report-file='${latteConfig.reportFile}'"} }
        latteConfig.exclude.isEmpty().let { if (!it) {latteConfigString += "--exclude='${latteConfig.exclude}'"} }
        latteConfig.test.isEmpty().let { if (!it) {latteConfigString += "--test='${latteConfig.test}'"} }
        latteConfig.suite.isEmpty().let { if (!it) {latteConfigString += "--suite='${latteConfig.suite}'"} }
        latteConfig.progress.isEmpty().let { if (!it) {latteConfigString += "--progress='${latteConfig.progress}'"} }
        latteConfig.domEnv.isEmpty().let { if (!it) {latteConfigString += "--dom-env='${latteConfig.domEnv}'"} }

        val scope = when (testScope) {
            ScopeType.DIRECTORY.id -> { arrayOf("--include='$testsDirectory/**/*.{test,spec}.{js,ts,jsx,tsx}'") }
            ScopeType.FILE.id      -> { arrayOf("--include='$testsFile'") }
            ScopeType.SUITE.id     -> { arrayOf("--include='$suiteFile'", "--suite='$suiteName'") }
            ScopeType.TEST.id      -> { arrayOf("--include='$testFile'", "--test='$testName'") }
            else                   -> { arrayOf("--include='${latteConfig.include}'") }
        }

        scope.forEach {
            latteConfigString += it
        }

        return latteConfigString
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return object : CommandLineState(environment) {
            @Throws(ExecutionException::class)
            override fun startProcess(): ProcessHandler {
                val latteConfig = getLatteConfig()
                val lattePath = (lattePath?: "").isEmpty().let { if (it) {Paths.get(project.basePath!!, "node_modules", "@olton", "latte").toString()} else {lattePath!!} }
                val latte = "$lattePath/cli/latte.js"

                val nodeExecutable = nodeInterpreter!!.ifEmpty { "node" }
                val commandLine = GeneralCommandLine()
                commandLine.exePath = nodeExecutable

                // Node.js options
                if (!nodeOptions.isNullOrEmpty()) {
                    nodeOptions!!.trim().split(" ").forEach { option ->
                        if (option.isNotEmpty()) {
                            commandLine.addParameter(option)
                        }
                    }
                }

                // Add latte executable
                commandLine.addParameter(latte)

                // Add latte options
                latteConfig?.forEach { param ->
                    commandLine.addParameter(param)
                }

                commandLine.charset = Charset.forName("UTF-8")
                commandLine.setWorkDirectory(workingDirectory ?: project.basePath ?: "")

                // Add environment variables
                if (envVariables.isNotEmpty()) {
                    commandLine.withEnvironment(envVariables)
                }

                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}