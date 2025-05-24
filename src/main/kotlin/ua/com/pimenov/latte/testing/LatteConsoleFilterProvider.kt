package ua.com.pimenov.latte.testing

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

class LatteConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(LatteConsoleFilter(project))
    }
}

class LatteConsoleFilter(private val project: Project) : Filter {
    companion object {
        private val TEST_OUTPUT_PATTERN = Regex("\\[LATTE\\] (.*?):(\\d+) - (.*)")
        private val TEAMCITY_PATTERN = Regex("##teamcity\\[(\\w+)(?: name='(.*?)')?\\]")
    }

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        // First try to match TeamCity format
        val teamcityMatch = TEAMCITY_PATTERN.find(line)
        if (teamcityMatch != null) {
            val eventType = teamcityMatch.groupValues[1]
            val testName = if (teamcityMatch.groupValues.size > 2) teamcityMatch.groupValues[2] else ""

            // Calculate the start and end offsets of the matched text in the console output
            val startOffset = entireLength - line.length
            val endOffset = startOffset + teamcityMatch.range.last + 1

            // For now, we're just highlighting the TeamCity messages without navigation
            // This can be enhanced later to navigate to the actual test file
            return Filter.Result(startOffset, endOffset, null)
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
