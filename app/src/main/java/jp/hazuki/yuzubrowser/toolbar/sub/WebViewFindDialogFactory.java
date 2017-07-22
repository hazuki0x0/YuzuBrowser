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

package jp.hazuki.yuzubrowser.toolbar.sub;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.theme.ThemeData;
import jp.hazuki.yuzubrowser.utils.ImeUtils;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebViewFindDialogFactory {
    public static WebViewFindDialog createInstance(Context context, View layout) {
        return new WebViewFind(context, layout);
    }

    private static class WebViewFind implements WebViewFindDialog {
        private final View root;
        private final Context mContext;
        private final EditText editText;
        private final TextView match;
        private CustomWebView mCurrentWeb = null;

        public WebViewFind(Context context, View view) {
            mContext = context;
            root = view;

            editText = (EditText) root.findViewById(R.id.findEditText);
            match = (TextView) root.findViewById(R.id.howMatchTextView);
        }

        @Override
        public void show(CustomWebView web) {
            mCurrentWeb = web;
            mCurrentWeb.setFindListener(findListener);

            ImageButton buttonLeft = (ImageButton) root.findViewById(R.id.buttonLeft);
            ImageButton buttonRight = (ImageButton) root.findViewById(R.id.buttonRight);
            ImageButton buttonEnd = (ImageButton) root.findViewById(R.id.buttonEnd);

            if (ThemeData.isEnabled()) {
                ThemeData data = ThemeData.getInstance();

                if (data.toolbarBackgroundColor != 0)
                    root.setBackgroundColor(data.toolbarBackgroundColor);
                else
                    root.setBackgroundResource(R.color.deep_gray);

                if (data.toolbarTextColor != 0) {
                    editText.setTextColor(data.toolbarTextColor);
                    editText.setHintTextColor(data.toolbarTextColor & 0xffffff | 0x88000000);
                    match.setTextColor(data.toolbarTextColor);
                } else {
                    editText.setTextColor(Color.WHITE);
                    editText.setHintTextColor(0x88ffffff);
                    match.setTextColor(Color.WHITE);
                }

                if (data.toolbarImageColor != 0) {
                    buttonLeft.setColorFilter(data.toolbarImageColor);
                    buttonRight.setColorFilter(data.toolbarImageColor);
                    buttonEnd.setColorFilter(data.toolbarImageColor);
                } else {
                    buttonLeft.clearColorFilter();
                    buttonRight.clearColorFilter();
                    buttonEnd.clearColorFilter();
                }
            }

            root.setVisibility(View.VISIBLE);
            editText.requestFocus();
            editText.postDelayed(new Runnable() {
                public void run() {
                    editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                    editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 100);
            editText.setText("");

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mCurrentWeb == null) return;

                    mCurrentWeb.clearMatches();
                    mCurrentWeb.findAllAsync(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            buttonLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            if (mCurrentWeb == null) return;

                            ImeUtils.hideIme(mContext, editText);
                            mCurrentWeb.findNext(false);
                            mCurrentWeb.requestFocus();
                        }
                    });

            buttonRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            if (mCurrentWeb == null) return;

                            ImeUtils.hideIme(mContext, editText);
                            mCurrentWeb.findNext(true);
                            mCurrentWeb.requestFocus();
                        }
                    });

            buttonEnd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            hide();
                        }
                    });
        }

        @Override
        public void hide() {
            root.setVisibility(View.GONE);
            ImeUtils.hideIme(mContext, editText);

            if (mCurrentWeb != null) {
                mCurrentWeb.clearMatches();
                mCurrentWeb.notifyFindDialogDismissedMethod();
            }
        }

        @Override
        public boolean isVisible() {
            return root.getVisibility() == View.VISIBLE;
        }

        private WebView.FindListener findListener = new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                if (match != null) {
                    match.setText((numberOfMatches > 0 ? activeMatchOrdinal + 1 : 0) + "/" + numberOfMatches);
                }
            }
        };
    }
}
