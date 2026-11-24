package com.tangkinhcac.app.feature.auth

import android.view.inputmethod.BaseInputConnection
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged

internal fun TextView.doAfterImeTextCommitted(action: () -> Unit) {
    doAfterTextChanged { text ->
        // Re-rendering an input while the IME is composing can discard Vietnamese diacritics.
        if (text == null || BaseInputConnection.getComposingSpanStart(text) < 0) {
            action()
        }
    }
}
