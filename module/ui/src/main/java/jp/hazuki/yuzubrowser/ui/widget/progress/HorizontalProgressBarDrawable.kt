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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable

import androidx.annotation.IntRange

internal class HorizontalProgressBarDrawable : SingleHorizontalProgressDrawable() {

    override fun onLevelChange(@IntRange(from = 0, to = LEVEL_MAX.toLong()) level: Int): Boolean {
        invalidateSelf()
        return true
    }

    override fun onDrawRect(canvas: Canvas, paint: Paint) {

        val level = level
        if (level == 0) {
            return
        }

        val saveCount = canvas.save()
        canvas.scale(level.toFloat() / LEVEL_MAX, 1f, SingleHorizontalProgressDrawable.RECT_BOUND.left, 0f)

        super.onDrawRect(canvas, paint)

        // Draw twice to emulate the background for secondary progress.
        super.onDrawRect(canvas, paint)

        canvas.restoreToCount(saveCount)
    }

    companion object {
        /**
         * Value from [Drawable.getLevel]
         */
        private const val LEVEL_MAX = 10000
    }
}
