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

package jp.hazuki.yuzubrowser.legacy.utils.graphics

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx

class DividerDrawable(context: Context) : Drawable() {

    private val width: Int = context.convertDpToPx(1)
    private val padding: Int = context.convertDpToPx(4)
    private val paint: Paint = Paint().apply {
        color = Color.TRANSPARENT
    }

    override fun draw(canvas: Canvas) {
        val rect = bounds
        canvas.drawRect(rect.left.toFloat(), (rect.top + padding).toFloat(), rect.right.toFloat(), (rect.bottom - padding).toFloat(), paint)
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidateSelf()
    }

    fun setWithTextColor(color: Int) {
        setColor(color and 0xffffff or 0x55000000)
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }
}
