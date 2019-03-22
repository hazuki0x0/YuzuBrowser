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

/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package jp.hazuki.yuzubrowser.ui.widget.progress

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

internal open class SingleHorizontalProgressDrawable : BasePaintDrawable() {


    override fun onPreparePaint(paint: Paint) {
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas, width: Int, height: Int, paint: Paint) {
        canvas.scale(width / RECT_BOUND.width(), height / RECT_BOUND.height())
        canvas.translate(RECT_BOUND.width() / 2, RECT_BOUND.height() / 2)

        onDrawRect(canvas, paint)
    }

    protected open fun onDrawRect(canvas: Canvas, paint: Paint) {
        canvas.drawRect(RECT_BOUND, paint)
    }

    companion object {
        @JvmStatic
        protected val RECT_BOUND = RectF(-180f, -1f, 180f, 1f)
        private val RECT_PADDED_BOUND = RectF(-180f, -4f, 180f, 4f)
    }
}
