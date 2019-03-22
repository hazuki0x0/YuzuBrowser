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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.widget.TintTypedArray
import androidx.core.graphics.drawable.TintAwareDrawable
import jp.hazuki.yuzubrowser.ui.R

/**
 * A [ProgressBar] subclass that handles tasks related to backported progress drawable.
 */
class MaterialProgressBar : ProgressBar {

    // This field remains false inside super class constructor.
    private val mSuperInitialized = true
    private val mProgressTintInfo = TintInfo()

    /**
     * Get the current drawable of this ProgressBar.
     *
     * @return The current drawable.
     */
    val currentDrawable: Drawable?
        get() = if (isIndeterminate) indeterminateDrawable else progressDrawable

    /**
     * @see ProgressBar.getProgressTintList
     */
    /**
     * @see ProgressBar.setProgressTintList
     */
    var supportProgressTintList: ColorStateList?
        get() = mProgressTintInfo.mProgressTint
        set(tint) {
            mProgressTintInfo.mProgressTint = tint
            mProgressTintInfo.mHasProgressTint = true

            applyPrimaryProgressTint()
        }

    /**
     * @see ProgressBar.getProgressTintMode
     */
    /**
     * @see ProgressBar.setProgressTintMode
     */
    var supportProgressTintMode: PorterDuff.Mode?
        get() = mProgressTintInfo.mProgressTintMode
        set(tintMode) {
            mProgressTintInfo.mProgressTintMode = tintMode
            mProgressTintInfo.mHasProgressTintMode = true

            applyPrimaryProgressTint()
        }

    /**
     * @see ProgressBar.getSecondaryProgressTintList
     */
    /**
     * @see ProgressBar.setSecondaryProgressTintList
     */
    var supportSecondaryProgressTintList: ColorStateList?
        get() = mProgressTintInfo.mSecondaryProgressTint
        set(tint) {
            mProgressTintInfo.mSecondaryProgressTint = tint
            mProgressTintInfo.mHasSecondaryProgressTint = true

            applySecondaryProgressTint()
        }

    /**
     * @see ProgressBar.getSecondaryProgressTintMode
     */
    /**
     * @see ProgressBar.setSecondaryProgressTintMode
     */
    var supportSecondaryProgressTintMode: PorterDuff.Mode?
        get() = mProgressTintInfo.mSecondaryProgressTintMode
        set(tintMode) {
            mProgressTintInfo.mSecondaryProgressTintMode = tintMode
            mProgressTintInfo.mHasSecondaryProgressTintMode = true

            applySecondaryProgressTint()
        }

    /**
     * @see ProgressBar.getProgressBackgroundTintList
     */
    /**
     * @see ProgressBar.setProgressBackgroundTintList
     */
    var supportProgressBackgroundTintList: ColorStateList?
        get() = mProgressTintInfo.mProgressBackgroundTint
        set(tint) {
            mProgressTintInfo.mProgressBackgroundTint = tint
            mProgressTintInfo.mHasProgressBackgroundTint = true

            applyProgressBackgroundTint()
        }

    /**
     * @see ProgressBar.getProgressBackgroundTintMode
     */
    /**
     * @see ProgressBar.setProgressBackgroundTintMode
     */
    var supportProgressBackgroundTintMode: PorterDuff.Mode?
        get() = mProgressTintInfo.mProgressBackgroundTintMode
        set(tintMode) {
            mProgressTintInfo.mProgressBackgroundTintMode = tintMode
            mProgressTintInfo.mHasProgressBackgroundTintMode = true

            applyProgressBackgroundTint()
        }

    /**
     * @see ProgressBar.getIndeterminateTintList
     */
    /**
     * @see ProgressBar.setIndeterminateTintList
     */
    var supportIndeterminateTintList: ColorStateList?
        get() = mProgressTintInfo.mIndeterminateTint
        set(tint) {
            mProgressTintInfo.mIndeterminateTint = tint
            mProgressTintInfo.mHasIndeterminateTint = true

            applyIndeterminateTint()
        }

    /**
     * @see ProgressBar.getIndeterminateTintMode
     */
    /**
     * @see ProgressBar.setIndeterminateTintMode
     */
    var supportIndeterminateTintMode: PorterDuff.Mode?
        get() = mProgressTintInfo.mIndeterminateTintMode
        set(tintMode) {
            mProgressTintInfo.mIndeterminateTintMode = tintMode
            mProgressTintInfo.mHasIndeterminateTintMode = true

            applyIndeterminateTint()
        }

    constructor(context: Context) : super(context) {

        init(null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        init(attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {

        init(attrs, defStyleAttr, defStyleRes)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {

        val context = context
        val a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.MaterialProgressBar, defStyleAttr, defStyleRes)
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_progressTint)) {
            mProgressTintInfo.mProgressTint = a.getColorStateList(
                    R.styleable.MaterialProgressBar_mpb_progressTint)
            mProgressTintInfo.mHasProgressTint = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_progressTintMode)) {
            mProgressTintInfo.mProgressTintMode = parseTintMode(a.getInt(
                    R.styleable.MaterialProgressBar_mpb_progressTintMode, -1), null)
            mProgressTintInfo.mHasProgressTintMode = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_secondaryProgressTint)) {
            mProgressTintInfo.mSecondaryProgressTint = a.getColorStateList(
                    R.styleable.MaterialProgressBar_mpb_secondaryProgressTint)
            mProgressTintInfo.mHasSecondaryProgressTint = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_secondaryProgressTintMode)) {
            mProgressTintInfo.mSecondaryProgressTintMode = parseTintMode(a.getInt(
                    R.styleable.MaterialProgressBar_mpb_secondaryProgressTintMode, -1), null)
            mProgressTintInfo.mHasSecondaryProgressTintMode = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_progressBackgroundTint)) {
            mProgressTintInfo.mProgressBackgroundTint = a.getColorStateList(
                    R.styleable.MaterialProgressBar_mpb_progressBackgroundTint)
            mProgressTintInfo.mHasProgressBackgroundTint = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_progressBackgroundTintMode)) {
            mProgressTintInfo.mProgressBackgroundTintMode = parseTintMode(a.getInt(
                    R.styleable.MaterialProgressBar_mpb_progressBackgroundTintMode, -1), null)
            mProgressTintInfo.mHasProgressBackgroundTintMode = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_indeterminateTint)) {
            mProgressTintInfo.mIndeterminateTint = a.getColorStateList(
                    R.styleable.MaterialProgressBar_mpb_indeterminateTint)
            mProgressTintInfo.mHasIndeterminateTint = true
        }
        if (a.hasValue(R.styleable.MaterialProgressBar_mpb_indeterminateTintMode)) {
            mProgressTintInfo.mIndeterminateTintMode = parseTintMode(a.getInt(
                    R.styleable.MaterialProgressBar_mpb_indeterminateTintMode, -1), null)
            mProgressTintInfo.mHasIndeterminateTintMode = true
        }
        a.recycle()

        progressDrawable = HorizontalProgressDrawable(context)
        indeterminateDrawable = IndeterminateHorizontalProgressDrawable(context)

        applyIndeterminateTint()
    }

    @Synchronized
    override fun setIndeterminate(indeterminate: Boolean) {
        super.setIndeterminate(indeterminate)

        if (mSuperInitialized && currentDrawable !is MaterialProgressDrawable) {
            Log.w(TAG, "Current drawable is not a MaterialProgressDrawable, you may want to set" + " app:mpb_setBothDrawables")
        }
    }

    override fun setProgressDrawable(drawable: Drawable?) {
        super.setProgressDrawable(drawable)

        // mProgressTintInfo can be null during super class initialization.
        if (mProgressTintInfo != null) {
            applyProgressTints()
        }
    }

    override fun setIndeterminateDrawable(drawable: Drawable?) {
        super.setIndeterminateDrawable(drawable)

        // mProgressTintInfo can be null during super class initialization.
        if (mProgressTintInfo != null) {
            applyIndeterminateTint()
        }
    }


    @Deprecated("Use {@link #getSupportProgressTintList()} instead.")
    override fun getProgressTintList(): ColorStateList? {
        logProgressBarTintWarning()
        return supportProgressTintList
    }


    @Deprecated("Use {@link #setSupportProgressTintList(ColorStateList)} instead.")
    override fun setProgressTintList(tint: ColorStateList?) {
        logProgressBarTintWarning()
        supportProgressTintList = tint
    }


    @Deprecated("Use {@link #getSupportProgressTintMode()} instead.")
    override fun getProgressTintMode(): PorterDuff.Mode? {
        logProgressBarTintWarning()
        return supportProgressTintMode
    }


    @Deprecated("Use {@link #setSupportProgressTintMode(PorterDuff.Mode)} instead.")
    override fun setProgressTintMode(tintMode: PorterDuff.Mode?) {
        logProgressBarTintWarning()
        supportProgressTintMode = tintMode
    }


    @Deprecated("Use {@link #getSupportSecondaryProgressTintList()} instead.")
    override fun getSecondaryProgressTintList(): ColorStateList? {
        logProgressBarTintWarning()
        return supportSecondaryProgressTintList
    }


    @Deprecated("Use {@link #setSupportSecondaryProgressTintList(ColorStateList)} instead.")
    override fun setSecondaryProgressTintList(tint: ColorStateList?) {
        logProgressBarTintWarning()
        supportSecondaryProgressTintList = tint
    }


    @Deprecated("Use {@link #getSupportSecondaryProgressTintMode()} instead.")
    override fun getSecondaryProgressTintMode(): PorterDuff.Mode? {
        logProgressBarTintWarning()
        return supportSecondaryProgressTintMode
    }


    @Deprecated("Use {@link #setSupportSecondaryProgressTintMode(PorterDuff.Mode)} instead.")
    override fun setSecondaryProgressTintMode(tintMode: PorterDuff.Mode?) {
        logProgressBarTintWarning()
        supportSecondaryProgressTintMode = tintMode
    }


    @Deprecated("Use {@link #getSupportProgressBackgroundTintList()} instead.")
    override fun getProgressBackgroundTintList(): ColorStateList? {
        logProgressBarTintWarning()
        return supportProgressBackgroundTintList
    }


    @Deprecated("Use {@link #setSupportProgressBackgroundTintList(ColorStateList)} instead.")
    override fun setProgressBackgroundTintList(tint: ColorStateList?) {
        logProgressBarTintWarning()
        supportProgressBackgroundTintList = tint
    }


    @Deprecated("Use {@link #getSupportProgressBackgroundTintMode()} instead.")
    override fun getProgressBackgroundTintMode(): PorterDuff.Mode? {
        logProgressBarTintWarning()
        return supportProgressBackgroundTintMode
    }


    @Deprecated("Use {@link #setSupportProgressBackgroundTintMode(PorterDuff.Mode)} instead.")
    override fun setProgressBackgroundTintMode(tintMode: PorterDuff.Mode?) {
        logProgressBarTintWarning()
        supportProgressBackgroundTintMode = tintMode
    }


    @Deprecated("Use {@link #getSupportIndeterminateTintList()} instead.")
    override fun getIndeterminateTintList(): ColorStateList? {
        logProgressBarTintWarning()
        return supportIndeterminateTintList
    }


    @Deprecated("Use {@link #setSupportIndeterminateTintList(ColorStateList)} instead.")
    override fun setIndeterminateTintList(tint: ColorStateList?) {
        logProgressBarTintWarning()
        supportIndeterminateTintList = tint
    }


    @Deprecated("Use {@link #getSupportIndeterminateTintMode()} instead.")
    override fun getIndeterminateTintMode(): PorterDuff.Mode? {
        logProgressBarTintWarning()
        return supportIndeterminateTintMode
    }


    @Deprecated("Use {@link #setSupportIndeterminateTintMode(PorterDuff.Mode)} instead.")
    override fun setIndeterminateTintMode(tintMode: PorterDuff.Mode?) {
        logProgressBarTintWarning()
        supportIndeterminateTintMode = tintMode
    }

    private fun logProgressBarTintWarning() {
        Log.w(TAG, "Non-support version of tint method called, this is error-prone and will crash" +
                " below Lollipop if you are calling it as a method of ProgressBar instead of" +
                " MaterialProgressBar")
    }

    private fun applyProgressTints() {
        if (progressDrawable == null) {
            return
        }
        applyPrimaryProgressTint()
        applyProgressBackgroundTint()
        applySecondaryProgressTint()
    }

    private fun applyPrimaryProgressTint() {
        if (progressDrawable == null) {
            return
        }
        if (mProgressTintInfo.mHasProgressTint || mProgressTintInfo.mHasProgressTintMode) {
            val target = getTintTargetFromProgressDrawable(android.R.id.progress, true)
            if (target != null) {
                applyTintForDrawable(target, mProgressTintInfo.mProgressTint,
                        mProgressTintInfo.mHasProgressTint, mProgressTintInfo.mProgressTintMode,
                        mProgressTintInfo.mHasProgressTintMode)
            }
        }
    }

    private fun applySecondaryProgressTint() {
        if (progressDrawable == null) {
            return
        }
        if (mProgressTintInfo.mHasSecondaryProgressTint || mProgressTintInfo.mHasSecondaryProgressTintMode) {
            val target = getTintTargetFromProgressDrawable(android.R.id.secondaryProgress,
                    false)
            if (target != null) {
                applyTintForDrawable(target, mProgressTintInfo.mSecondaryProgressTint,
                        mProgressTintInfo.mHasSecondaryProgressTint,
                        mProgressTintInfo.mSecondaryProgressTintMode,
                        mProgressTintInfo.mHasSecondaryProgressTintMode)
            }
        }
    }

    private fun applyProgressBackgroundTint() {
        if (progressDrawable == null) {
            return
        }
        if (mProgressTintInfo.mHasProgressBackgroundTint || mProgressTintInfo.mHasProgressBackgroundTintMode) {
            val target = getTintTargetFromProgressDrawable(android.R.id.background, false)
            if (target != null) {
                applyTintForDrawable(target, mProgressTintInfo.mProgressBackgroundTint,
                        mProgressTintInfo.mHasProgressBackgroundTint,
                        mProgressTintInfo.mProgressBackgroundTintMode,
                        mProgressTintInfo.mHasProgressBackgroundTintMode)
            }
        }
    }

    private fun getTintTargetFromProgressDrawable(layerId: Int, shouldFallback: Boolean): Drawable? {
        val progressDrawable = progressDrawable ?: return null
        progressDrawable.mutate()
        var layerDrawable: Drawable? = null
        if (progressDrawable is LayerDrawable) {
            layerDrawable = progressDrawable.findDrawableByLayerId(layerId)
        }
        if (layerDrawable == null && shouldFallback) {
            layerDrawable = progressDrawable
        }
        return layerDrawable
    }

    private fun applyIndeterminateTint() {
        val indeterminateDrawable = indeterminateDrawable ?: return
        if (mProgressTintInfo.mHasIndeterminateTint || mProgressTintInfo.mHasIndeterminateTintMode) {
            indeterminateDrawable.mutate()
            applyTintForDrawable(indeterminateDrawable, mProgressTintInfo.mIndeterminateTint,
                    mProgressTintInfo.mHasIndeterminateTint,
                    mProgressTintInfo.mIndeterminateTintMode,
                    mProgressTintInfo.mHasIndeterminateTintMode)
        }
    }

    // Progress drawables in this library has already rewritten tint related methods for
    // compatibility.
    @SuppressLint("NewApi")
    private fun applyTintForDrawable(drawable: Drawable, tint: ColorStateList?,
                                     hasTint: Boolean, tintMode: PorterDuff.Mode?,
                                     hasTintMode: Boolean) {

        if (hasTint || hasTintMode) {

            if (hasTint) {
                if (drawable is TintAwareDrawable) {

                    (drawable as TintAwareDrawable).setTintList(tint)
                } else {
                    logDrawableTintWarning()
                    drawable.setTintList(tint)
                }
            }

            if (hasTintMode && tintMode != null) {
                if (drawable is TintAwareDrawable) {

                    (drawable as TintAwareDrawable).setTintMode(tintMode)
                } else {
                    logDrawableTintWarning()
                    drawable.setTintMode(tintMode)
                }
            }

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (drawable.isStateful) {
                drawable.state = drawableState
            }
        }
    }

    private fun logDrawableTintWarning() {
        Log.w(TAG, "Drawable did not implement TintableDrawable, it won't be tinted below" + " Lollipop")
    }

    private class TintInfo {

        var mProgressTint: ColorStateList? = null
        var mProgressTintMode: PorterDuff.Mode? = null
        var mHasProgressTint: Boolean = false
        var mHasProgressTintMode: Boolean = false

        var mSecondaryProgressTint: ColorStateList? = null
        var mSecondaryProgressTintMode: PorterDuff.Mode? = null
        var mHasSecondaryProgressTint: Boolean = false
        var mHasSecondaryProgressTintMode: Boolean = false

        var mProgressBackgroundTint: ColorStateList? = null
        var mProgressBackgroundTintMode: PorterDuff.Mode? = null
        var mHasProgressBackgroundTint: Boolean = false
        var mHasProgressBackgroundTintMode: Boolean = false

        var mIndeterminateTint: ColorStateList? = null
        var mIndeterminateTintMode: PorterDuff.Mode? = null
        var mHasIndeterminateTint: Boolean = false
        var mHasIndeterminateTintMode: Boolean = false
    }

    companion object {

        private val TAG = MaterialProgressBar::class.java.simpleName

        fun parseTintMode(value: Int,
                          defaultMode: PorterDuff.Mode?): PorterDuff.Mode? {
            return when (value) {
                3 -> PorterDuff.Mode.SRC_OVER
                5 -> PorterDuff.Mode.SRC_IN
                9 -> PorterDuff.Mode.SRC_ATOP
                14 -> PorterDuff.Mode.MULTIPLY
                15 -> PorterDuff.Mode.SCREEN
                16 -> PorterDuff.Mode.ADD
                else -> defaultMode
            }
        }
    }
}
