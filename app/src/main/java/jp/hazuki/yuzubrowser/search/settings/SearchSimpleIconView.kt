/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.search.settings

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import jp.hazuki.yuzubrowser.R

class SearchSimpleIconView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.oval_icon_background)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
    }

    fun setSearchUrl(searchUrl: SearchUrl?) {
        if (searchUrl == null) {
            text = ""
            setIconColor(ColorGenerator.getRandomColor())
            return
        }

        if (searchUrl.color != 0) {
            setIconColor(searchUrl.color)
        } else {
            if (searchUrl.title.isNotEmpty()) {
                setIconColor(ColorGenerator.getColor(searchUrl.title))
            } else {
                setIconColor(ColorGenerator.getRandomColor())
            }
        }

        text = if (searchUrl.title.isNotEmpty())
            String(Character.toChars(searchUrl.title.codePointAt(0))) else ""
    }

    private fun setIconColor(color: Int) {
        background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        if (isColorLight(color)) {
            setTextColor(Color.BLACK)
        } else {
            setTextColor(Color.WHITE)
        }
    }

    private fun isColorLight(color: Int): Boolean {
        val lightness = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return lightness > 0.7
    }
}