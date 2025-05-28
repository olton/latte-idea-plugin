package ua.com.pimenov.latte.testing

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestFinder
import ua.com.pimenov.latte.utils.isTestFile

class LatteTestFinder : TestFinder {
    override fun findSourceElement(element: PsiElement): PsiElement? {
        return element.containingFile
    }

    override fun findTestsForClass(element: PsiElement): Collection<PsiElement> {
        // Логіка для пошуку тестів для конкретного класу
        return emptyList()
    }

    override fun findClassesForTest(element: PsiElement): Collection<PsiElement> {
        // Логіка для пошуку класів, які тестуються даним тестом
        return emptyList()
    }

    override fun isTest(element: PsiElement): Boolean {
        if (element is PsiFile) {
            val latteTestFramework = LatteTestFramework()
            return isTestFile(element as VirtualFile)
        }
        return false
    }
}