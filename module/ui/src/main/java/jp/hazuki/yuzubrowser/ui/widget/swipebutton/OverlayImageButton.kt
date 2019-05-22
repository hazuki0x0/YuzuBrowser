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

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

open class OverlayImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatImageButton(context, attrs) {
    var overlay: Drawable? = null
        set(value) {
            if (field != value) {
                val old = field
                field = value

                if (value != null) {
                    value.callback = this
                    value.colorFilter = colorFilter

                    if (old != null) {
                        value.bounds = old.bounds
                    }
                }

                old?.callback = null
                invalidate()
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val overlay = overlay ?: return

        if (paddingTop == 0 && paddingLeft == 0) {
            overlay.draw(canvas)
        } else {
            val saveCount = canvas.saveCount
            canvas.save()

            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
            overlay.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        super.setColorFilter(cf)
        overlay?.colorFilter = colorFilter
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        configureBounds()
        return changed
    }

    private fun configureBounds() {
        val overlay = overlay ?: return
        val vWidth = width - paddingLeft - paddingRight
        val vHeight = height - paddingTop - paddingBottom

        overlay.setBounds(0, 0, vWidth, vHeight)
    }

    override fun verifyDrawable(dr: Drawable): Boolean {
        return overlay == dr || super.verifyDrawable(dr)
    }

    override fun invalidateDrawable(dr: Drawable) {
        if (dr == overlay) {
            invalidate()
        } else {
            super.invalidateDrawable(dr)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(parentWidth, parentHeight)

        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
