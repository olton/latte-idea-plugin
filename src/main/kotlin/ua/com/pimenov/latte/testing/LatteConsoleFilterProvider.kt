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
        private val LATTE_OUTPUT_PATTERN = Regex("\\[LATTE] (.*?):(\\d+) - (.*)")
    }

    // Get the test status manager
    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        // НЕ обробляємо TeamCity повідомлення - це робить SMTestRunner
        if (line.contains("##teamcity")) {
            return null
        }

        // Обробляємо тільки звичайний вивід Latte (якщо такий є)
        val latteMatch = LATTE_OUTPUT_PATTERN.find(line)
        if (latteMatch != null) {
            val filePath = latteMatch.groupValues[1]
            val lineNumber = latteMatch.groupValues[2].toIntOrNull() ?: 1
            val message = latteMatch.groupValues[3]

            val startOffset = entireLength - line.length
            val endOffset = startOffset + line.length

            val file = File(filePath)
            if (file.exists()) {
                return Filter.Result(
                    startOffset,
                    endOffset
                ) { project ->
                    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
                    if (virtualFile != null) {
                        FileEditorManager.getInstance(project).openTextEditor(
                            OpenFileDescriptor(project, virtualFile, lineNumber - 1, 0),
                            true
                        )
                    }
                }
            }
        }

        return null
    }
}
