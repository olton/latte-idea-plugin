package ua.com.pimenov.latte.testing

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.fileEditor.FileEditorManager
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
                "testSuiteStarted", "testStarted", "testFinished", "testFailed", "testIgnored" -> {
                    // For test-related messages, highlight the entire message
                    return Filter.Result(startOffset, endOffset, null)
                }
                else -> {
                    // For other TeamCity messages, highlight just the command part
                    return Filter.Result(startOffset, endOffset, null)
                }
            }
        }

        // If not TeamCity format, try the original LATTE format
        val match = TEST_OUTPUT_PATTERN.find(line) ?: return null

        val filePath = match.groupValues[1]
        val lineNumber = match.groupValues[2].toIntOrNull() ?: 0
        val message = match.groupValues[3]

        // Calculate the start and end offsets of the matched text in the console output
        val startOffset = entireLength - line.length
        val endOffset = startOffset + match.range.last + 1

        // Create a hyperlink to the test location
        return Filter.Result(
            startOffset,
            endOffset,
            object : HyperlinkInfo {
                override fun navigate(project: Project) {
                    // Create a location string in the format expected by LatteTestLocationProvider
                    val locationString = "$filePath::$message"

                    // Use the LatteTestLocationProvider to find the location
                    val locations = LatteTestLocationProvider.INSTANCE.getLocation(
                        LatteTestLocationProvider.PROTOCOL_ID,
                        locationString,
                        project,
                        GlobalSearchScope.projectScope(project)
                    )

                    // Navigate to the first location found
                    if (locations.isNotEmpty()) {
                        // Instead of trying to navigate to the location directly,
                        // we'll open the file using FileEditorManager
                        val file = File(filePath)
                        if (file.exists()) {
                            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
                            if (virtualFile != null) {
                                FileEditorManager.getInstance(project).openFile(virtualFile, true)
                            }
                        }
                    }
                }
            }
        )
    }
}
