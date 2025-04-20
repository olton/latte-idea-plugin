package ua.com.pimenov.latte.utils

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object LatteIcons {
    @JvmField
    val LatteIcon: Icon = IconLoader.getIcon("/icons/latte.svg", LatteIcons::class.java)

    // Можете додати інші іконки, якщо потрібно
//    @JvmField
//    val RunAll: Icon = IconLoader.getIcon("/icons/run_all.svg", LatteIcons::class.java)
}