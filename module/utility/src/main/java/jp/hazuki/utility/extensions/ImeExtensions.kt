package jp.hazuki.utility.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Context.hideIme(editText: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(editText.windowToken, 0)
}

fun Context.showIme(editText: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.isImeShown(): Boolean {
    val rect = Rect()
    window.decorView.getWindowVisibleDisplayFrame(rect)
    if (rect.top != 0 || rect.bottom != 0) {
        if (rect.bottom - rect.top < getDisplayHeight() * 0.7)
            return true
    }
    return false
}