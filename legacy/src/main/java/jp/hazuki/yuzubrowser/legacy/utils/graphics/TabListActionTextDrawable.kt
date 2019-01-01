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
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToFloatPx
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx

class TabListActionTextDrawable(context: Context, tabs: Int) : Drawable() {

    private val text: String = if (tabs > 99) ":D" else Integer.toString(tabs)
    private val paint: Paint = Paint()
    private val paddingY: Int = context.convertDpToPx(2)

    init {
        paint.textSize = context.convertDpToFloatPx(10)
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
    }

    override fun draw(canvas: Canvas) {
        val r = Rect()
        paint.getTextBounds(text, 0, text.length, r)
        val bounds = bounds

        val xPos = bounds.left + bounds.width() / 2
        val yPos = bounds.top + bounds.height() / 2 + r.height() / 2 + paddingY
        paint.textSize
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), paint)
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        if (bottom - top < paint.textSize * 2) {
            paint.textSize = (bottom - top) * 5 / 12f
        }
    }
}
