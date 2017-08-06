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

package jp.hazuki.yuzubrowser.utils.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

public class FullFillTextView extends AppCompatTextView {

    private CharSequence content = "";
    private CharSequence visibleText;

    public FullFillTextView(Context context) {
        this(context, null);
    }

    public FullFillTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLines(1);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        content = text != null ? text : "";
        setContentDescription(content);
        updateVisibleText(0, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int availWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        updateVisibleText(availWidth,
                MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private CharSequence getTruncatedText(int availWidth) {
        // Build the full string, which should fit within availWidth.
        return TextUtils.ellipsize(content, getPaint(), availWidth, TextUtils.TruncateAt.END);
    }

    private void updateVisibleText(int availWidth, boolean unspecifiedWidth) {
        CharSequence newText;
        if (unspecifiedWidth) {
            newText = content;
        } else {
            newText = getTruncatedText(availWidth);
        }

        if (!newText.equals(visibleText)) {
            visibleText = newText;

            super.setText(visibleText, BufferType.SPANNABLE);
        }
    }
}
