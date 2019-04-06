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
 * Copyright (c) 2017 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package jp.hazuki.yuzubrowser.ui.widget.progress

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.TintAwareDrawable
import jp.hazuki.yuzubrowser.ui.R
import jp.hazuki.yuzubrowser.ui.extensions.getColorFromAttrRes
import jp.hazuki.yuzubrowser.ui.extensions.getFloatFromAttrRes

@Suppress("LeakingThis", "UNCHECKED_CAST")
internal open class BaseProgressLayerDrawable<BackgroundDrawableType : TintAwareDrawable, ProgressDrawableType : TintAwareDrawable>(layers: Array<Drawable>, context: Context) : LayerDrawable(layers), MaterialProgressDrawable, TintAwareDrawable {

    private val backgroundAlpha: Float = context.getFloatFromAttrRes(android.R.attr.disabledAlpha, 0f)

    private val backgroundDrawable: BackgroundDrawableType
    private val secondaryProgressDrawable: ProgressDrawableType
    private val progressDrawable: ProgressDrawableType

    init {
        setId(0, android.R.id.background)

        backgroundDrawable = getDrawable(0) as BackgroundDrawableType
        setId(1, android.R.id.secondaryProgress)

        secondaryProgressDrawable = getDrawable(1) as ProgressDrawableType
        setId(2, android.R.id.progress)

        progressDrawable = getDrawable(2) as ProgressDrawableType

        val controlActivatedColor = context.getColorFromAttrRes(R.attr.colorControlActivated, Color.BLACK)
        setTint(controlActivatedColor)
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("NewApi")
    override fun setTint(@ColorInt tintColor: Int) {
        // Modulate alpha of tintColor against mBackgroundAlpha.
        val backgroundTintColor = ColorUtils.setAlphaComponent(tintColor, Math.round(Color.alpha(
                tintColor) * backgroundAlpha))
        backgroundDrawable.setTint(backgroundTintColor)
        secondaryProgressDrawable.setTint(backgroundTintColor)
        progressDrawable.setTint(tintColor)
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("NewApi")
    override fun setTintList(tint: ColorStateList?) {
        val backgroundTint: ColorStateList?
        if (tint != null) {
            if (!tint.isOpaque) {
                Log.w(javaClass.simpleName, "setTintList() called with a non-opaque" + " ColorStateList, its original alpha will be discarded")
            }
            backgroundTint = tint.withAlpha(Math.round(backgroundAlpha * 255))
        } else {
            backgroundTint = null
        }
        backgroundDrawable.setTintList(backgroundTint)
        secondaryProgressDrawable.setTintList(backgroundTint)
        progressDrawable.setTintList(tint)
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("NewApi")
    override fun setTintMode(tintMode: PorterDuff.Mode) {
        backgroundDrawable.setTintMode(tintMode)
        secondaryProgressDrawable.setTintMode(tintMode)
        progressDrawable.setTintMode(tintMode)
    }
}
