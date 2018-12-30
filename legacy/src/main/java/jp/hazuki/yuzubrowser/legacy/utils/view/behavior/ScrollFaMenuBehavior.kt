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

package jp.hazuki.yuzubrowser.legacy.utils.view.behavior

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.github.clans.fab.FloatingActionMenu

class ScrollFaMenuBehavior(context: Context, attrs: AttributeSet) : FloatingActionMenu.Behavior(context, attrs) {

    companion object {
        private val INTERPOLATOR = AccelerateDecelerateInterpolator()
        private const val DURATION_TIME = 500L
    }

    private var translationY = 0f
    private var isAnimating = false

    override fun onStartNestedScroll(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout, child: FloatingActionMenu, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == View.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout, child: FloatingActionMenu, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)

        if (dyConsumed > 0 && !isAnimating) {
            animateOut(child)
        } else if (dyConsumed < 0 && !isAnimating) {
            animateIn(child)
        }
    }

    private fun animateOut(view: View) {
        if (translationY == 0f) {
            translationY = getTranslationY(view).toFloat()
        }

        view.animate().run {
            translationY(translationY)
            duration = DURATION_TIME
            interpolator = INTERPOLATOR
            setListener(animateListener)
        }
    }

    private fun animateIn(view: View) {
        view.animate().run {
            translationY(0f)
            duration = DURATION_TIME
            interpolator = INTERPOLATOR
            setListener(animateListener)
        }
    }

    private val animateListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            isAnimating = false
        }

        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            isAnimating = true
        }
    }

    private fun getTranslationY(view: View): Int {
        val param = view.layoutParams as ViewGroup.MarginLayoutParams
        return view.height + param.bottomMargin
    }
}