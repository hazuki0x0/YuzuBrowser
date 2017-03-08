package jp.hazuki.yuzubrowser.toolbar.sub;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
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

            root.setVisibility(View.VISIBLE);
            ImeUtils.showIme(mContext, editText);
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

            root.findViewById(R.id.buttonLeft)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            if (mCurrentWeb == null) return;

                            ImeUtils.hideIme(mContext, editText);
                            mCurrentWeb.findNext(false);
                            mCurrentWeb.requestFocus();
                        }
                    });
            root.findViewById(R.id.buttonRight)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            if (mCurrentWeb == null) return;

                            ImeUtils.hideIme(mContext, editText);
                            mCurrentWeb.findNext(true);
                            mCurrentWeb.requestFocus();
                        }
                    });
            root.findViewById(R.id.buttonEnd)
                    .setOnClickListener(new View.OnClickListener() {
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
                    match.setText(activeMatchOrdinal + "/" + numberOfMatches);
                }
            }
        };
    }
}
