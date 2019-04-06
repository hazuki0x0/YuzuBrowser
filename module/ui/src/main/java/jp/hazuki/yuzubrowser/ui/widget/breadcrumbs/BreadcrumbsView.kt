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
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.ui.R

class BreadcrumbsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @StyleRes defStyle: Int = R.style.BreadCrumbsViewStyle) : RecyclerView(context, attrs, defStyle) {

    val linearLayoutManager = LinearLayoutManager(context).apply {
        orientation = LinearLayoutManager.HORIZONTAL
    }

    internal val currentTextColor: Int
    internal val otherTextColor: Int
    private val arrowColor: Int

    var listener: OnBreadcrumbsViewClickListener? = null

    init {
        layoutManager = linearLayoutManager

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.BreadcrumbsView, 0, 0)
        currentTextColor = a.getColor(R.styleable.BreadcrumbsView_crumbsCurrentItemTextColor, Color.WHITE)
        otherTextColor = a.getColor(R.styleable.BreadcrumbsView_crumbsOtherItemTextColor, 0xB3000000.toInt() and Color.WHITE)
        arrowColor = a.getColor(R.styleable.BreadcrumbsView_crumbsArrowColor, otherTextColor)
        a.recycle()

        addItemDecoration(BreadcrumbItemDecoration(context, arrowColor))
    }


    interface Breadcrumb {
        val title: CharSequence
    }

    interface OnBreadcrumbsViewClickListener {
        fun onBreadcrumbItemClick(position: Int)
    }
}