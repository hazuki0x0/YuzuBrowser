/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.core.utility.extensions

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