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

// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package jp.hazuki.yuzubrowser.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ScrollView
import android.widget.TextView


@SuppressLint("ViewConstructor")
/**
 * Context menu title text view that is restricted height and scrollable.
 * @param context Context to be used to inflate this view.
 * @param title String to be displayed as the title.
 */
class ContextMenuTitleView(context: Context, title: String) : ScrollView(context) {

    private val mDpToPx: Float

    init {
        var titleText = title
        mDpToPx = getContext().resources.displayMetrics.density
        val padding = (PADDING_DP * mDpToPx).toInt()
        setPadding(padding, padding, padding, 0)
        overScrollMode = View.OVER_SCROLL_NEVER

        val titleView = TextView(context)
        if (!TextUtils.isEmpty(titleText) && titleText.length > MAX_TITLE_CHARS) {
            val sb = StringBuilder(MAX_TITLE_CHARS + ELLIPSIS.length)
            sb.append(titleText, 0, MAX_TITLE_CHARS)
            sb.append(ELLIPSIS)
            titleText = sb.toString()
        }
        titleView.text = titleText
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorPrimary, outValue, true)
        titleView.setTextColor(outValue.data)
        android.R.attr.textColorPrimary
        titleView.setPadding(0, 0, 0, padding)
        addView(titleView)
    }

    override fun onMeasure(widthMeasureSpec: Int, defHeightMeasureSpec: Int) {
        val maxHeight = (MAX_HEIGHT_DP * mDpToPx).toInt()
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    companion object {
        private const val MAX_HEIGHT_DP = 70
        private const val PADDING_DP = 16
        private const val MAX_TITLE_CHARS = 1024
        private const val ELLIPSIS = "\u2026"
    }
}