/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.R
import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.view.doOnNextLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class LongSummaryPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        (holder.findViewById(R.id.summary) as TextView).apply {
            val filter = TextFilter(this)
            maxLines = 2
            filters = arrayOf(filter)
            ellipsize = TextUtils.TruncateAt.END
            doOnNextLayout {
                text = text
            }
        }
    }

    private class TextFilter(
        private val textView: TextView,
    ) : InputFilter {
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
            val paint = textView.paint
            val width = textView.width - textView.compoundPaddingLeft - textView.compoundPaddingRight

            val result = SpannableStringBuilder()
            var currentStart = start
            for (index in currentStart until end) {
                if (Layout.getDesiredWidth(source, currentStart, index + 1, paint) > width) {
                    result.append(source.subSequence(currentStart, index)).append('\n')
                    currentStart = index
                }
            }

            if (start < end) {
                result.append(source.subSequence(start, end))
            }

            return result
        }
    }
}
