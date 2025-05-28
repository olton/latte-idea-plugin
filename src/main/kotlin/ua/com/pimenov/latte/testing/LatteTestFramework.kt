package ua.com.pimenov.latte.testing

import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestFramework
import ua.com.pimenov.latte.utils.LatteIcons
import javax.swing.Icon
import ua.com.pimenov.latte.utils.isTestFile

class LatteTestFramework : TestFramework {
    companion object {
        val INSTANCE: LatteTestFramework = LatteTestFramework()
    }

    override fun getName(): String = "Latte"

    override fun getIcon(): Icon = LatteIcons.LatteIcon

    override fun isLibraryAttached(module: Module): Boolean = true

    override fun getLibraryPath(): String? = null

    override fun isTestClass(clazz: PsiElement): Boolean = false

    override fun isTestMethod(element: PsiElement): Boolean = false

    override fun getDefaultSuperClass(): String? = null

    override fun isPotentialTestClass(element: PsiElement): Boolean = false

    override fun findSetUpMethod(element: PsiElement): PsiElement? = null

    override fun findTearDownMethod(element: PsiElement): PsiElement? = null

    override fun findOrCreateSetUpMethod(element: PsiElement): PsiElement? = null

    override fun getSetUpMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Latte SetUp Method")

    override fun getTearDownMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Latte TearDown Method")

    override fun getTestMethodFileTemplateDescriptor(): FileTemplateDescriptor = FileTemplateDescriptor("Latte Test Method")

    override fun isIgnoredMethod(element: PsiElement): Boolean = false



    fun isApplicableFor(file: PsiFile): Boolean {
        val fileType = file.fileType
        return (fileType === JavaScriptSupportLoader.JAVASCRIPT || 
                fileType === JavaScriptSupportLoader.TYPESCRIPT) && isTestFile(file as VirtualFile)
    }

    override fun getLanguage() = JavaScriptSupportLoader.JAVASCRIPT.language
}
