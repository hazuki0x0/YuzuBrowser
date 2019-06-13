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

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class PaddingFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var realHeight = 0
        private set
    private var isBlackMode = false

    var visible = false
        set(value) {
            field = value
            setVisible()
        }

    var forceHide = false
        set(value) {
            field = value
            setVisible()
        }

    fun setBlackColorMode(blackMode: Boolean) {
        if (blackMode) {
            if (!isBlackMode) {
                setBackgroundColor(Color.BLACK)
                isBlackMode = true
            }
        } else {
            if (isBlackMode) {
                setBackgroundColor(Color.WHITE)
                isBlackMode = false
            }
        }
    }

    fun setHeight(height: Int) {
        if (realHeight != height) {
            realHeight = height
            val params = layoutParams
            params.height = height
            layoutParams = params
        }
    }

    private fun setVisible() {
        visibility = when {
            forceHide -> View.GONE
            visible -> View.VISIBLE
            else -> View.GONE
        }
    }
}
