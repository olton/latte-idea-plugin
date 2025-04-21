package ua.com.pimenov.latte.utils

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.Gray
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JTextField

fun setPlaceholder(field: JTextField, placeholderText: String) {
    applyPlaceholder(field, placeholderText)
}

fun setPlaceholder(field: TextFieldWithBrowseButton, placeholderText: String) {
    applyPlaceholder(field.textField, placeholderText)
}

private fun applyPlaceholder(field: JTextField, placeholderText: String) {
    val originalColor = field.foreground
    val placeholderColor: Gray? = Gray._150

    field.addFocusListener(object : FocusAdapter() {
        override fun focusGained(e: FocusEvent) {
            if (field.text == placeholderText) {
                field.text = ""
                field.foreground = originalColor
            }
        }

        override fun focusLost(e: FocusEvent) {
            if (field.text.isEmpty()) {
                field.text = placeholderText
                field.foreground = placeholderColor
                println(field.text)
            }
        }
    })
}
