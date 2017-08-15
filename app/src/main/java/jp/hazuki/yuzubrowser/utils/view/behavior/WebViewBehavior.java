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

package jp.hazuki.yuzubrowser.utils.view.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    private CustomWebView webView;
    private int prevY;
    private View topToolBar;
    private View webFrame;

    public WebViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        topToolBar = parent.findViewById(R.id.topToolbarLayout);
        webFrame = child.findViewById(R.id.webFrameLayout);
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        int bottom = dependency.getBottom();

        if (webView != null && !webView.isTouching()) {
            webView.scrollBy(0, bottom - prevY);
        }

        prevY = bottom;

        int toolbarHeight = topToolBar.getHeight();

        if (toolbarHeight != 0 && dependency.getHeight() == dependency.getBottom()) {
            webFrame.setPadding(0, 0, 0, toolbarHeight);
        } else {
            webFrame.setPadding(0, 0, 0, 0);
        }

        return super.onDependentViewChanged(parent, child, dependency);
    }

    public void setWebView(CustomWebView webView) {
        this.webView = webView;
    }
}
