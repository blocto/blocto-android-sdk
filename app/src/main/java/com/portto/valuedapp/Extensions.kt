package com.portto.valuedapp

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import com.github.razir.progressbutton.DrawableButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.portto.sdk.wallet.flow.CompositeSignature
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun EditText.textChanges(): Flow<CharSequence?> = callbackFlow {
    val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
    awaitClose { removeTextChangedListener(listener) }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun Button.showLoading() {
    this.showProgress {
        progressColor = Color.WHITE
        gravity = DrawableButton.GRAVITY_CENTER
    }
    this.isEnabled = false
}

fun Button.hideLoading(text: String) {
    this.hideProgress(text)
    this.isEnabled = true
}


/**
 * Make the provided address shorter for displaying
 */
fun String.shortenAddress(): String = "${substring(0, 6)}...${substring(length - 6, length)}"

/**
 * Map Flow [CompositeSignature] to string for displaying
 */
fun List<CompositeSignature>.mapToString() = joinToString("\n\n") {
    "Address: ${it.address}\nKey ID: ${it.keyId}\nSignature: ${it.signature}"
}