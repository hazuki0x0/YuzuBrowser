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
import android.graphics.Color
import android.graphics.Paint

internal abstract class BasePaintDrawable : BaseDrawable() {

    private var mPaint: Paint? = null

    override fun onDraw(canvas: Canvas, width: Int, height: Int) {
        val paint = mPaint ?: Paint().also {
            mPaint = it
            it.isAntiAlias = true
            it.color = Color.BLACK
            onPreparePaint(it)
        }
        paint.alpha = mAlpha
        paint.colorFilter = colorFilterForDrawing

        onDraw(canvas, width, height, paint)
    }

    protected abstract fun onPreparePaint(paint: Paint)

    protected abstract fun onDraw(canvas: Canvas, width: Int, height: Int,
                                  paint: Paint)
}
