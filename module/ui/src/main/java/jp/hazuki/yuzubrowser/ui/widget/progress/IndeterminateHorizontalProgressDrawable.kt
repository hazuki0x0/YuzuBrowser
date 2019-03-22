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
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package jp.hazuki.yuzubrowser.ui.widget.progress

import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

import jp.hazuki.yuzubrowser.ui.extensions.getFloatFromAttrRes


/**
 * A backported `Drawable` for indeterminate horizontal `ProgressBar`.
 */
/**
 * Create a new `IndeterminateHorizontalProgressDrawable`.
 *
 * @param context the `Context` for retrieving style information.
 */
internal class IndeterminateHorizontalProgressDrawable(context: Context) : BaseIndeterminateProgressDrawable(context), MaterialProgressDrawable {


    private val mBackgroundAlpha = context.getFloatFromAttrRes(android.R.attr.disabledAlpha, 0f)

    private val rect1TransformX = RectTransformX(RECT_1_TRANSFORM_X)
    private val rect2TransformX = RectTransformX(RECT_2_TRANSFORM_X)

    override var animators: Array<Animator> = arrayOf(
            Animators.createIndeterminateHorizontalRect1(rect1TransformX),
            Animators.createIndeterminateHorizontalRect2(rect2TransformX))

    override fun onPreparePaint(paint: Paint) {
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas, width: Int, height: Int, paint: Paint) {
        canvas.scale(width / RECT_BOUND.width(), height / RECT_BOUND.height())
        canvas.translate(RECT_BOUND.width() / 2, RECT_BOUND.height() / 2)

        paint.alpha = Math.round(mAlpha * mBackgroundAlpha)
        drawBackgroundRect(canvas, paint)
        paint.alpha = mAlpha

        drawProgressRect(canvas, rect2TransformX, paint)
        drawProgressRect(canvas, rect1TransformX, paint)
    }

    private fun drawBackgroundRect(canvas: Canvas, paint: Paint) {
        canvas.drawRect(RECT_BOUND, paint)
    }

    private fun drawProgressRect(canvas: Canvas, transformX: RectTransformX, paint: Paint) {

        val saveCount = canvas.save()
        canvas.translate(transformX.translateX, 0f)
        canvas.scale(transformX.scaleX, 1f)

        canvas.drawRect(RECT_PROGRESS, paint)

        canvas.restoreToCount(saveCount)
    }

    companion object {
        private val RECT_BOUND = RectF(-180f, -1f, 180f, 1f)
        private val RECT_PROGRESS = RectF(-144f, -1f, 144f, 1f)
        private val RECT_1_TRANSFORM_X = RectTransformX(-522.6f, 0.1f)
        private val RECT_2_TRANSFORM_X = RectTransformX(-197.6f, 0.1f)
    }
}
