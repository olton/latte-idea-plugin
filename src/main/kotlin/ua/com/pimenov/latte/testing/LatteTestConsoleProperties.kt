package ua.com.pimenov.latte.testing

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.ui.ConsoleView

class LatteTestConsoleProperties(
    configuration: RunConfiguration,
    executor: Executor,
    private val testFrameworkName: String = "Latte"
) : SMTRunnerConsoleProperties(configuration, testFrameworkName, executor) {

    override fun getTestLocator(): SMTestLocator {
        return LatteTestLocationProvider.INSTANCE
    }

    companion object {
        fun createConsole(
            testFrameworkName: String,
            configuration: RunConfiguration,
            executor: Executor
        ): ConsoleView {
            val properties = LatteTestConsoleProperties(configuration, executor, testFrameworkName)
            return SMTestRunnerConnectionUtil.createConsole(testFrameworkName, properties)
        }
    }
}