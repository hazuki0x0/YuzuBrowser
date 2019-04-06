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

// Copyright 2015 The Chromium Authors. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//    * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//    * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package jp.hazuki.yuzubrowser.legacy.utils.view.templatepreserving

import android.content.Context
import android.text.TextUtils
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

/**
 * A [AppCompatTextView] that truncates content within a template, instead of truncating
 * the template text. Truncation only happens if maxLines is set to 1 and there's not enough space
 * to display the entire content.
 *
 *
 * For example, given the following template and content
 * Template: "%s was closed"
 * Content: "https://www.google.com/webhp?sourceid=chrome-instant&q=potato"
 *
 *
 * the TemplatePreservingTextView would truncate the content but not the template text:
 * "https://www.google.com/webh... was closed"
 */
class TemplatePreservingTextView
/**
 * Builds an instance of an [TemplatePreservingTextView].
 *
 * @param context A [Context] instance to build this [TextView] in.
 * @param attrs   An [AttributeSet] instance.
 */
(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    private var mTemplate: String? = null
    private var mContent: CharSequence = ""
    private var mVisibleText: CharSequence? = null

    /**
     * Sets the template format string. setText() must be called after calling this method for the
     * new template text to take effect.
     *
     * @param template Template format string (e.g. "Closed %s"), or null. If null is passed, this
     * view acts like a normal TextView.
     */
    fun setTemplateText(template: String) {
        mTemplate = if (TextUtils.isEmpty(template)) null else template
    }

    /**
     * This will take `text` and apply it to the internal template, building a new
     * [String] to set.  This `text` will be automatically truncated to fit within
     * the template as best as possible, making sure the template does not get clipped.
     */
    override fun setText(text: CharSequence?, type: TextView.BufferType) {
        mContent = text ?: ""
        contentDescription = if (mTemplate == null) mContent else String.format(mTemplate!!, mContent)
        updateVisibleText(0, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        updateVisibleText(availWidth,
                MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getTruncatedText(availWidth: Int): CharSequence {
        val paint = paint

        // Calculate the width the template takes.
        val emptyTemplate = String.format(mTemplate!!, "")
        val emptyTemplateWidth = paint.measureText(emptyTemplate)

        // Calculate the available width for the content.
        val contentWidth = Math.max(availWidth - emptyTemplateWidth, 0f)

        // Ellipsize the content to the available width.
        val clipped = TextUtils.ellipsize(mContent, paint, contentWidth, TruncateAt.END)

        // Build the full string, which should fit within availWidth.
        return String.format(mTemplate!!, clipped)
    }

    private fun updateVisibleText(availWidth: Int, unspecifiedWidth: Boolean) {
        val visibleText: CharSequence = if (mTemplate == null) {
            mContent
        } else if (maxLines != 1 || unspecifiedWidth) {
            String.format(mTemplate!!, mContent)
        } else {
            getTruncatedText(availWidth)
        }

        if (visibleText != mVisibleText) {
            mVisibleText = visibleText

            // BufferType.SPANNABLE is required so that TextView.getIterableTextForAccessibility()
            // doesn't call our custom setText(). See crbug.com/449311
            super.setText(mVisibleText, TextView.BufferType.SPANNABLE)
        }
    }
}
