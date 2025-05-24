package ua.com.pimenov.latte.testing

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

class LatteTestLocationProvider : SMTestLocator {
    companion object {
        val INSTANCE = LatteTestLocationProvider()
        const val PROTOCOL_ID = "latte"
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

        // Parse the path to extract file path and test name
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

        // For now, just return the file location
        // In a more advanced implementation, you would locate the specific test within the file
        return listOf(PsiLocation(project, psiFile))
    }
}