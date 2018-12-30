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
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange

class LauncherIconDrawable(private val icon: Drawable) : Drawable() {

    override fun draw(canvas: Canvas) {
        icon.draw(canvas)
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        icon.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int {
        return icon.opacity
    }

    override fun getIntrinsicWidth(): Int {
        return icon.intrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return icon.intrinsicHeight
    }

    override fun getAlpha(): Int {
        return icon.alpha
    }

    override fun setState(stateSet: IntArray): Boolean {
        return icon.setState(stateSet)
    }

    override fun getConstantState(): Drawable.ConstantState? {
        return icon.constantState
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        icon.setBounds(left, top, right, bottom)
    }

    override fun applyTheme(t: Resources.Theme) {
        icon.applyTheme(t)
    }
}
