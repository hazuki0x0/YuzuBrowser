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

package jp.hazuki.yuzubrowser.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;

import org.jetbrains.annotations.NotNull;

public abstract class JvmWebViewBridge extends WebView implements CustomWebView {

    public JvmWebViewBridge(Context context) {
        super(context);
    }

    public JvmWebViewBridge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JvmWebViewBridge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getWebScrollX() {
        return getScrollX();
    }

    @Override
    public int getWebScrollY() {
        return getScrollY();
    }

    @NotNull
    @Override
    public WebView getWebView() {
        return this;
    }

    @NotNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean getSwipeEnable() {
        return false;
    }

    @Override
    public void setSwipeEnable(boolean b) {
    }

    @Override
    public boolean requestWebFocus() {
        return requestFocus();
    }

    @Override
    public boolean saveWebArchiveMethod(@NotNull String filename) {
        saveWebArchive(filename);
        return true;
    }

    @Override
    public boolean setOverScrollModeMethod(int arg) {
        setOverScrollMode(arg);
        return true;
    }

    @Override
    public void setAcceptThirdPartyCookies(@NotNull CookieManager manager, boolean accept) {
        manager.setAcceptThirdPartyCookies(this, accept);
    }

    @Override
    public boolean isRedirect() {
        return CustomWebView.DefaultImpls.isRedirect(this);
    }

    @Override
    public boolean isInvertMode() {
        return CustomWebView.DefaultImpls.isInvertMode(this);
    }

    @Override
    public boolean isNestedScrollingEnabledMethod() {
        return isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabledMethod(boolean b) {
        setNestedScrollingEnabled(b);
    }

    @Override
    public int computeVerticalScrollRangeMethod() {
        return computeVerticalScrollRange();
    }

    @Override
    public int computeVerticalScrollOffsetMethod() {
        return computeVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtentMethod() {
        return computeVerticalScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRangeMethod() {
        return computeHorizontalScrollRange();
    }

    @Override
    public int computeHorizontalScrollOffsetMethod() {
        return computeHorizontalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtentMethod() {
        return computeHorizontalScrollExtent();
    }
}
