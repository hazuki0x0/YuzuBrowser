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

package jp.hazuki.yuzubrowser.ui.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class FullFillTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {

    private var content: CharSequence = ""
    private var visibleText: CharSequence? = null

    init {
        setLines(1)
    }

    override fun setText(text: CharSequence?, type: BufferType) {
        content = text ?: ""
        contentDescription = content
        updateVisibleText(0, true)
    }

    override fun getText(): CharSequence {
        return content
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        updateVisibleText(availWidth,
                MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getTruncatedText(availWidth: Int): CharSequence {
        // Build the full string, which should fit within availWidth.
        return TextUtils.ellipsize(content, paint, availWidth.toFloat(), TextUtils.TruncateAt.END)
    }

    private fun updateVisibleText(availWidth: Int, unspecifiedWidth: Boolean) {
        val newText = if (unspecifiedWidth) content else getTruncatedText(availWidth)

        if (newText != visibleText) {
            visibleText = newText

            super.setText(visibleText, BufferType.SPANNABLE)
        }
    }
}
