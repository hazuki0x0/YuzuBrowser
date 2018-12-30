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

package jp.hazuki.yuzubrowser.legacy.utils.graphics

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange

class SimpleLayerDrawable(private vararg val drawables: Drawable) : Drawable() {

    override fun draw(canvas: Canvas) {
        for (d in drawables)
            d.draw(canvas)
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        for (d in drawables)
            d.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        for (d in drawables)
            d.colorFilter = colorFilter
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        for (d in drawables)
            d.setBounds(left, top, right, bottom)
    }

    override fun applyTheme(t: Resources.Theme) {
        super.applyTheme(t)
        for (d in drawables)
            d.applyTheme(t)
    }

    override fun getIntrinsicWidth(): Int {
        return drawables
                .map { it.intrinsicWidth }
                .max()
                ?: -1
    }

    override fun getIntrinsicHeight(): Int {
        return drawables
                .map { it.intrinsicHeight }
                .max()
                ?: -1
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
