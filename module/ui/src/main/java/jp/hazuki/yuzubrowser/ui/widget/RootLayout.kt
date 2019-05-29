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

package jp.hazuki.yuzubrowser.ui.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.core.utility.extensions.isImeShown
import jp.hazuki.yuzubrowser.ui.R

class RootLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {
    private var isWhiteMode = false

    private var onImeShownListener: ((Boolean) -> Unit)? = null


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onImeShownListener?.invoke((context as Activity).isImeShown())

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setOnImeShownListener(l: (visible: Boolean) -> Unit) {
        onImeShownListener = l
    }

    fun setWhiteBackgroundMode(whiteMode: Boolean) {
        if (isWhiteMode != whiteMode) {
            isWhiteMode = whiteMode
            if (whiteMode) {
                setBackgroundColor(Color.WHITE)
            } else {
                setBackgroundColor(context.getResColor(R.color.browserBackground))
            }
        }
    }
}
