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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Path

/**
 * Animators backported for Drawables in this library.
 */
internal object Animators {

    // M -522.59998,0
    // c 48.89972,0 166.02656,0 301.21729,0
    // c 197.58128,0 420.9827,0 420.9827,0
    private val PATH_INDETERMINATE_HORIZONTAL_RECT1_TRANSLATE_X = Path().apply {
        moveTo(-522.59998f, 0f)
        rCubicTo(48.89972f, 0f, 166.02656f, 0f, 301.21729f, 0f)
        rCubicTo(197.58128f, 0f, 420.9827f, 0f, 420.9827f, 0f)

    }

    // M 0 0.1
    // L 1 0.826849212646
    // L 2 0.1
    private val PATH_INDETERMINATE_HORIZONTAL_RECT1_SCALE_X = Path().apply {
        moveTo(0f, 0.1f)
        lineTo(1f, 0.826849212646f)
        lineTo(2f, 0.1f)
    }

    // M -197.60001,0
    // c 14.28182,0 85.07782,0 135.54689,0
    // c 54.26191,0 90.42461,0 168.24331,0
    // c 144.72154,0 316.40982,0 316.40982,0
    private val PATH_INDETERMINATE_HORIZONTAL_RECT2_TRANSLATE_X = Path().apply {
        moveTo(-197.60001f, 0f)
        rCubicTo(14.28182f, 0f, 85.07782f, 0f, 135.54689f, 0f)
        rCubicTo(54.26191f, 0f, 90.42461f, 0f, 168.24331f, 0f)
        rCubicTo(144.72154f, 0f, 316.40982f, 0f, 316.40982f, 0f)
    }

    // M 0.0,0.1
    // L 1.0,0.571379510698
    // L 2.0,0.909950256348
    // L 3.0,0.1
    private val PATH_INDETERMINATE_HORIZONTAL_RECT2_SCALE_X: Path = Path().apply {
        moveTo(0f, 0.1f)
        lineTo(1f, 0.571379510698f)
        lineTo(2f, 0.909950256348f)
        lineTo(3f, 0.1f)
    }

    /**
     * Create a backported Animator for
     * `@android:anim/progress_indeterminate_horizontal_rect1`.
     *
     * @param target The object whose properties are to be animated.
     * @return An Animator object that is set up to behave the same as the its native counterpart.
     */
    fun createIndeterminateHorizontalRect1(target: RectTransformX): Animator {

        val translateXAnimator = ObjectAnimator.ofFloat(target, "translateX", null,
                PATH_INDETERMINATE_HORIZONTAL_RECT1_TRANSLATE_X)
        translateXAnimator.duration = 2000
        translateXAnimator.interpolator = IndeterminateInterpolator.INDETERMINATE_HORIZONTAL_RECT1_TRANSLATE_X
        translateXAnimator.repeatCount = ValueAnimator.INFINITE

        val scaleXAnimator = ObjectAnimator.ofFloat(target, null, "scaleX",
                PATH_INDETERMINATE_HORIZONTAL_RECT1_SCALE_X)
        scaleXAnimator.duration = 2000
        scaleXAnimator.interpolator = IndeterminateInterpolator.INDETERMINATE_HORIZONTAL_RECT1_SCALE_X
        scaleXAnimator.repeatCount = ValueAnimator.INFINITE

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateXAnimator, scaleXAnimator)
        return animatorSet
    }

    /**
     * Create a backported Animator for
     * `@android:anim/progress_indeterminate_horizontal_rect2`.
     *
     * @param target The object whose properties are to be animated.
     * @return An Animator object that is set up to behave the same as the its native counterpart.
     */
    fun createIndeterminateHorizontalRect2(target: RectTransformX): Animator {

        val translateXAnimator = ObjectAnimator.ofFloat(target, "translateX", null,
                PATH_INDETERMINATE_HORIZONTAL_RECT2_TRANSLATE_X)
        translateXAnimator.duration = 2000
        translateXAnimator.interpolator = IndeterminateInterpolator.INDETERMINATE_HORIZONTAL_RECT2_TRANSLATE_X
        translateXAnimator.repeatCount = ValueAnimator.INFINITE

        val scaleXAnimator = ObjectAnimator.ofFloat(target, null, "scaleX",
                PATH_INDETERMINATE_HORIZONTAL_RECT2_SCALE_X)
        scaleXAnimator.duration = 2000
        scaleXAnimator.interpolator = IndeterminateInterpolator.INDETERMINATE_HORIZONTAL_RECT2_SCALE_X
        scaleXAnimator.repeatCount = ValueAnimator.INFINITE

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateXAnimator, scaleXAnimator)
        return animatorSet
    }
}
