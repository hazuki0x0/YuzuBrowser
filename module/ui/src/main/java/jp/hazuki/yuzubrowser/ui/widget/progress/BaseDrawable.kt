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

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.drawable.TintAwareDrawable

internal abstract class BaseDrawable : Drawable(), TintAwareDrawable {

    @IntRange(from = 0, to = 255)
    protected var mAlpha = 255
    protected var mColorFilter: ColorFilter? = null
    protected var mTintList: ColorStateList? = null
    protected var mTintMode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN
    protected var mTintFilter: PorterDuffColorFilter? = null

    private val mConstantState = DummyConstantState()

    protected val colorFilterForDrawing: ColorFilter?
        get() = if (mColorFilter != null) mColorFilter else mTintFilter

    override fun getAlpha(): Int {
        return mAlpha
    }

    /**
     * {@inheritDoc}
     */
    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        if (mAlpha != alpha) {
            mAlpha = alpha
            invalidateSelf()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getColorFilter(): ColorFilter? {
        return mColorFilter
    }

    /**
     * {@inheritDoc}
     */
    override fun setColorFilter(colorFilter: ColorFilter?) {
        mColorFilter = colorFilter
        invalidateSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun setTint(@ColorInt tintColor: Int) {
        setTintList(ColorStateList.valueOf(tintColor))
    }

    /**
     * {@inheritDoc}
     */
    override fun setTintList(tint: ColorStateList?) {
        mTintList = tint
        if (updateTintFilter()) {
            invalidateSelf()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setTintMode(tintMode: PorterDuff.Mode) {
        mTintMode = tintMode
        if (updateTintFilter()) {
            invalidateSelf()
        }
    }

    override fun isStateful(): Boolean {
        return mTintList != null && mTintList!!.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        return updateTintFilter()
    }

    private fun updateTintFilter(): Boolean {

        val tintList = mTintList
        if (tintList == null) {
            val hadTintFilter = mTintFilter != null
            mTintFilter = null
            return hadTintFilter
        }

        val tintColor = tintList.getColorForState(state, Color.TRANSPARENT)
        // They made PorterDuffColorFilter.setColor() and setMode() @hide.
        mTintFilter = PorterDuffColorFilter(tintColor, mTintMode)
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun getOpacity(): Int {
        // Be safe.
        return PixelFormat.TRANSLUCENT
    }

    /**
     * {@inheritDoc}
     */
    override fun draw(canvas: Canvas) {

        val bounds = bounds
        if (bounds.width() == 0 || bounds.height() == 0) {
            return
        }

        val saveCount = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        onDraw(canvas, bounds.width(), bounds.height())
        canvas.restoreToCount(saveCount)
    }

    protected abstract fun onDraw(canvas: Canvas, width: Int, height: Int)

    // Workaround LayerDrawable.ChildDrawable which calls getConstantState().newDrawable()
    // without checking for null.
    // We are never inflated from XML so the protocol of ConstantState does not apply to us. In
    // order to make LayerDrawable happy, we return ourselves from DummyConstantState.newDrawable().

    override fun getConstantState(): Drawable.ConstantState {
        return mConstantState
    }

    private inner class DummyConstantState : Drawable.ConstantState() {

        override fun getChangingConfigurations(): Int {
            return 0
        }

        override fun newDrawable(): Drawable {
            return this@BaseDrawable
        }
    }
}
