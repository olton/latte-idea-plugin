package ua.com.pimenov.latte.utils

import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JTextField

// Константи
private val PLACEHOLDER_COLOR = Gray._150
private val NORMAL_COLOR = JBColor.BLACK

// Установка плейсхолдера для звичайного JTextField
fun setPlaceholder(field: JTextField, placeholderText: String) {
    val originalColor = field.foreground

    field.text = placeholderText
    field.foreground = PLACEHOLDER_COLOR

    field.addFocusListener(object : FocusAdapter() {
        override fun focusGained(e: FocusEvent) {
            if (field.text == placeholderText) {
                field.text = ""
                field.foreground = originalColor
            }
        }

        override fun focusLost(e: FocusEvent) {
            if (field.text.isEmpty()) {
                field.foreground = PLACEHOLDER_COLOR
                field.text = placeholderText
            }
        }
    })
}