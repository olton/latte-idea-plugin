package ua.com.pimenov.latte.utils

import com.intellij.ui.Gray
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JTextField

fun setPlaceholder(field: JTextField, placeholderText: String) {
    val originalColor = field.foreground
    val placeholderColor = Gray._150

    if (field.text.isEmpty()) {
        field.text = placeholderText
        field.foreground = placeholderColor
    }

    field.addFocusListener(object : FocusAdapter() {
        override fun focusGained(e: FocusEvent) {
            if (field.text == placeholderText) {
                field.text = ""
                field.foreground = originalColor
            }
        }

        override fun focusLost(e: FocusEvent) {
            if (field.text.isEmpty()) {
                field.foreground = placeholderColor
                field.text = placeholderText
            }
        }
    })
}