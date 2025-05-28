package ua.com.pimenov.latte.testing

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.search.GlobalSearchScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


fun formatTimestamp(timestamp: String): String {
    try {
        val timestampLong = timestamp.toLong()
        val date = Date(timestampLong)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    } catch (e: Exception) {
        // Якщо не вдалося розпарсити, повертаємо поточну дату
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}


class LatteConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(LatteConsoleFilter(project))
    }
}

class LatteConsoleFilter(private val project: Project) : Filter {
    companion object {
        private val TEST_OUTPUT_PATTERN = Regex("\\[LATTE\\] (.*?):(\\d+) - (.*)")
        private val TEAMCITY_PATTERN = Regex("##teamcity\\[(\\w+)(?:\\s+[\\w\\-]+=(?:'[^']*'|\"[^\"]*\"))*\\s*\\]")
        private val TEAMCITY_PARAM_PATTERN = Regex("(\\w+)=(?:'([^']*)'|\"([^\"]*)\")")
    }

    // Get the test status manager
    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        // First try to match TeamCity format
        val teamcityMatch = TEAMCITY_PATTERN.find(line)
        if (teamcityMatch != null) {
            val eventType = teamcityMatch.groupValues[1]

            // Extract all parameters from the TeamCity message
            val params = mutableMapOf<String, String>()
            val paramMatches = TEAMCITY_PARAM_PATTERN.findAll(line)
            for (paramMatch in paramMatches) {
                val paramName = paramMatch.groupValues[1]
                // The value could be in either group 2 (single quotes) or group 3 (double quotes)
                val paramValue = paramMatch.groupValues[2].ifEmpty { paramMatch.groupValues[3] }
                params[paramName] = paramValue
            }

            // Get common parameters
            val name = params["name"] ?: ""
            val message = params["message"] ?: ""
            val details = params["details"] ?: ""
            val flowId = params["flowId"] ?: ""
            val timestamp = params["timestamp"] ?: ""
            val nodeId = params["nodeId"] ?: ""
            val parentNodeId = params["parentNodeId"] ?: ""

            // Get test-specific parameters
            val duration = params["duration"] ?: ""
            val locationHint = params["locationHint"] ?: ""
            val captureStandardOutput = params["captureStandardOutput"] ?: ""
            val testType = params["testType"] ?: ""
            val status = params["status"] ?: ""

            // Get error-specific parameters
            val errorDetails = params["errorDetails"] ?: ""
            val actual = params["actual"] ?: ""
            val expected = params["expected"] ?: ""
            val type = params["type"] ?: ""
            val stackTrace = params["stackTrace"] ?: ""
            val comparisonFailure = params["comparisonFailure"] ?: ""

            // Get artifact-specific parameters
            val path = params["path"] ?: ""
            val size = params["size"] ?: ""

            // Get coverage-specific parameters
            val coverageStats = params["coverageStats"] ?: ""
            val coverageClass = params["coverageClass"] ?: ""
            val coverageMethod = params["coverageMethod"] ?: ""
            val coverageBlock = params["coverageBlock"] ?: ""
            val coverageLine = params["coverageLine"] ?: ""

            // Calculate the start and end offsets of the matched text in the console output
            val startOffset = entireLength - line.length
            val endOffset = startOffset + line.length

            // Process different types of TeamCity messages
            when (eventType) {
                "testFailed" -> {
                    // Update test status to failed
                    if (locationHint.isNotEmpty()) {
                        val fileLocationMatch = Regex("file:///(.*?):(\\d+):(\\d+)").find(locationHint)
                        if (fileLocationMatch != null) {
                            val filePath = fileLocationMatch.groupValues[1]
                            val lineNumber = fileLocationMatch.groupValues[2].toIntOrNull() ?: 1
                            var colNumber = fileLocationMatch.groupValues[3].toIntOrNull() ?: 1

                            return Filter.Result(
                                startOffset,
                                endOffset,
                                object : HyperlinkInfo {
                                    override fun navigate(project: Project) {
                                        // For failed tests, we want to navigate to the test in the TestExplorer
                                        // The TestExplorer uses the LatteTestLocationProvider to find the test
                                        // We can use the same approach to find the test and navigate to it

                                        // First, try to find the test in the file
                                        val file = File(filePath)
                                        if (file.exists()) {
                                            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
                                            if (virtualFile != null) {
                                                // Navigate to the specific line in the file
                                                // This will open the file and show the test
                                                FileEditorManager.getInstance(project).openTextEditor(
                                                    OpenFileDescriptor(project, virtualFile, lineNumber - 1, colNumber - 1),
                                                    true
                                                )

                                                // The TestExplorer will automatically select the test
                                                // when the file is opened at the test location
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    // If no locationHint or couldn't parse it, just highlight the message
                    return Filter.Result(startOffset, endOffset, null)
                }
                "testFinished" -> {
                    // For finished tests, update the test status to passed if there's a locationHint
                    if (locationHint.isNotEmpty() && status == "passed") {
                        val fileLocationMatch = Regex("file:///(.*?):(\\d+):(\\d+)").find(locationHint)
                        if (fileLocationMatch != null) {
                            val filePath = fileLocationMatch.groupValues[1]
                        }
                    }
                    return Filter.Result(startOffset, endOffset, null)
                }
                "testSuiteStarted", "testStarted", "testIgnored" -> {
                    // For other test-related messages, highlight the entire message
                    return Filter.Result(startOffset, endOffset, null)
                }
                else -> {
                    // For other TeamCity messages, highlight just the command part
                    return Filter.Result(startOffset, endOffset, null)
                }
            }
        }

        // If not TeamCity format, return 0
        return Filter.Result(0, 0, null)
    }
}
