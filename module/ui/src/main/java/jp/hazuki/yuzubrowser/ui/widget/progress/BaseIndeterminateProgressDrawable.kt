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
import android.graphics.Color
import android.graphics.drawable.Animatable
import jp.hazuki.yuzubrowser.ui.R
import jp.hazuki.yuzubrowser.ui.extensions.getColorFromAttrRes

internal abstract class BaseIndeterminateProgressDrawable(context: Context) : BasePaintDrawable(), Animatable {

    protected abstract var animators: Array<Animator>

    private val isStarted: Boolean
        get() {
            for (animator in animators) {
                if (animator.isStarted) {
                    return true
                }
            }
            return false
        }

    init {
        val controlActivatedColor = context.getColorFromAttrRes(R.attr.colorControlActivated, Color.BLACK)
        // setTint() has been overridden for compatibility; DrawableCompat won't work because
        // wrapped Drawable won't be Animatable.
        setTint(controlActivatedColor)
    }

    /**
     * {@inheritDoc}
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (isStarted) {
            invalidateSelf()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun start() {

        if (isStarted) {
            return
        }

        for (animator in animators) {
            animator.start()
        }
        invalidateSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun stop() {
        for (animator in animators) {
            animator.end()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun isRunning(): Boolean {
        for (animator in animators) {
            if (animator.isRunning) {
                return true
            }
        }
        return false
    }
}
