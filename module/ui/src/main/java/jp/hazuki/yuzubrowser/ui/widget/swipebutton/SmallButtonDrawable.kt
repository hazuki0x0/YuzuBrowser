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

package jp.hazuki.yuzubrowser.ui.widget.swipebutton

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import kotlin.math.min

class SmallButtonDrawable(private val icon: Drawable, private val maxSize: Int) : Drawable() {

    override fun draw(canvas: Canvas) {
        if (isVisible) {
            icon.draw(canvas)
        }
    }

    override fun setAlpha(alpha: Int) {
        icon.alpha = alpha
    }

    override fun getOpacity() = icon.opacity

    override fun setColorFilter(colorFilter: ColorFilter?) {
        icon.colorFilter = colorFilter
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        val width = ((right - left) / 3f + 0.5f).toInt()
        val height = ((bottom - top) / 3f + 0.5f).toInt()

        val size = min(min(width, height), maxSize)

        icon.setBounds(right - size, bottom - size, right, bottom)
    }

    override fun getIntrinsicWidth() = icon.intrinsicWidth

    override fun getIntrinsicHeight() = icon.intrinsicHeight
}
