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

package jp.hazuki.yuzubrowser.utils.view.templatepreserving;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;

public class TemplatePreservingSnackBar extends BaseTransientBottomBar<TemplatePreservingSnackBar> {


    private TemplatePreservingTextView textView;
    private TextView action;

    private boolean isDismissByAction = false;

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent              The parent for this transient bottom bar.
     * @param content             The content view for this transient bottom bar.
     * @param contentViewCallback The content view callback for this transient bottom bar.
     */
    private TemplatePreservingSnackBar(@NonNull ViewGroup parent, @NonNull View content, @NonNull ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);

        textView = content.findViewById(R.id.snackbarText);
        action = content.findViewById(R.id.snackbarAction);
    }

    public static TemplatePreservingSnackBar make(@NonNull ViewGroup view, String template, CharSequence title, @Duration int duration) {
        ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View content = inflater.inflate(R.layout.template_preserving_snackbar, parent, false);

        ContentViewCallback callback = new ContentViewCallback(content);
        TemplatePreservingSnackBar snackBar = new TemplatePreservingSnackBar(parent, content, callback);
        snackBar.setDuration(duration);
        snackBar.setTemplateText(template);
        snackBar.setText(title);
        return snackBar;
    }

    public void setTemplateText(String text) {
        textView.setTemplateText(text);
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    @NonNull
    public TemplatePreservingSnackBar setAction(@StringRes int text, final View.OnClickListener listener) {
        return setAction(getContext().getText(text), listener);
    }

    @NonNull
    public TemplatePreservingSnackBar setAction(CharSequence text, final View.OnClickListener listener) {
        final TextView tv = action;

        if (TextUtils.isEmpty(text) || listener == null) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    // Now dismiss the Snackbar
                    isDismissByAction = true;
                    dismiss();
                }
            });
        }
        return this;
    }

    public boolean isDismissByAction() {
        return isDismissByAction;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    private static class ContentViewCallback implements BaseTransientBottomBar.ContentViewCallback {

        private View content;

        ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            // add custom *in animations for your views
            // e.g. original snackbar uses alpha animation, from 0 to 1
            content.setScaleY(0f);
            content.animate()
                    .scaleY(1f)
                    .setDuration(duration)
                    .setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            // add custom *out animations for your views
            // e.g. original snackbar uses alpha animation, from 1 to 0
            content.setScaleY(1f);
            content.animate()
                    .scaleY(0f)
                    .setDuration(duration)
                    .setStartDelay(delay);
        }
    }
}
