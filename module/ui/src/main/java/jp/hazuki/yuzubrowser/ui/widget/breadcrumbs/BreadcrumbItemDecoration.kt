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

package jp.hazuki.yuzubrowser.ui.widget.breadcrumbs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.ui.R


class BreadcrumbItemDecoration(context: Context, color: Int) : RecyclerView.ItemDecoration() {
    private val icon: Drawable = context.getDrawable(R.drawable.ic_chevron_right_white_24dp)!!
    private val leftPadding = context.convertDpToPx(8)

    init {
        icon.setTint(color)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val pos = parent.getChildAdapterPosition(view)
        outRect.left = if (pos != 0) {
            icon.intrinsicWidth
        } else {
            leftPadding
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val padding = (parent.height - icon.intrinsicHeight) / 2
        val arrowTop = parent.paddingTop + padding
        val arrowBottom = arrowTop + icon.intrinsicHeight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val arrowLeft = child.right + params.rightMargin
            val arrowRight = arrowLeft + icon.intrinsicWidth

            icon.setBounds(arrowLeft, arrowTop, arrowRight, arrowBottom)
            icon.draw(c)
        }
    }
}