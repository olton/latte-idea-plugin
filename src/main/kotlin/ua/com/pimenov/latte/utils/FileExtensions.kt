package ua.com.pimenov.latte.utils

import com.intellij.openapi.vfs.VirtualFile

val TEST_FILE_EXTENSIONS = setOf("test.js", "test.jsx", "test.ts", "test.tsx", "spec.js", "spec.jsx", "spec.ts", "spec.tsx")

fun isTestFile(file: VirtualFile): Boolean {
    val fileName = file.name
    return TEST_FILE_EXTENSIONS.any { fileName.endsWith(it) }
}