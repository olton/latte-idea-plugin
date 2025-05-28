package ua.com.pimenov.latte.testing

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

class LatteTestLocationProvider : SMTestLocator {
    companion object {
        val INSTANCE = LatteTestLocationProvider()
        const val PROTOCOL_ID = "teamcity"
    }

    override fun getLocation(
        protocol: String,
        path: String,
        project: Project,
        scope: GlobalSearchScope
    ): List<Location<*>> {
        if (protocol != PROTOCOL_ID) {
            return emptyList()
        }

        // Check if the path is in the format filename:line:column or filename::testName
        val isLineColumnFormat = path.contains(":") && !path.contains("::")

        if (isLineColumnFormat) {
            // Parse the path in the format filename:line:column
            val parts = path.split(":")
            if (parts.size < 2) {
                return emptyList()
            }

            val filePath = parts[0]
            val line = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val column = parts.getOrNull(2)?.toIntOrNull() ?: 0

            // Find the file in the project
            val file = File(filePath)
            if (!file.exists()) {
                return emptyList()
            }

            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: return emptyList()
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return emptyList()

            if (psiFile !is JSFile) {
                return emptyList()
            }

            // Get the document for the file
            val document = FileDocumentManager.getInstance().getDocument(virtualFile)
            if (document != null && line < document.lineCount) {
                // Convert line and column to offset
                val offset = document.getLineStartOffset(line) + minOf(column, document.getLineEndOffset(line) - document.getLineStartOffset(line))

                // Find the element at the offset
                val element = psiFile.findElementAt(offset)
                if (element != null) {
                    return listOf(PsiLocation(project, element))
                }
            }

            // Fallback to file location if we couldn't find the element
            return listOf(PsiLocation(project, psiFile))
        } else {
            // Parse the path to extract file path and test name (original implementation)
            val parts = path.split("::")
            if (parts.isEmpty()) {
                return emptyList()
            }

            val filePath = parts[0]
            val testName = if (parts.size > 1) parts[1] else null

            // Find the file in the project
            val file = File(filePath)
            if (!file.exists()) {
                return emptyList()
            }

            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: return emptyList()
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return emptyList()

            if (psiFile !is JSFile) {
                return emptyList()
            }

            // Return the file location
            return listOf(PsiLocation(project, psiFile))
        }
    }
}
