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

package jp.hazuki.yuzubrowser.legacy.tab.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.pattern.action.WebSettingResetAction;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;
import jp.hazuki.yuzubrowser.ui.theme.ThemeData;
import jp.hazuki.yuzubrowser.webview.CustomWebView;

public class MainTabData extends TabData {
    public MainTabData(CustomWebView web, View view) {
        super(web);
        mTabView = view;
        titleTextView = view.findViewById(R.id.textView);
        context = view.getContext();
        loadingIcon = (AnimatedVectorDrawable) context.getDrawable(R.drawable.ic_loading_circle_24dp);
    }

    public MainTabData(CustomWebView web, View view, TabIndexData data) {
        super(web, data);
        mTabView = view;
        titleTextView = view.findViewById(R.id.textView);
        context = view.getContext();
        loadingIcon = (AnimatedVectorDrawable) context.getDrawable(R.drawable.ic_loading_circle_24dp);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        super.onPageStarted(url, favicon);
        setText(url);
        finished = false;
        startDocument = true;
        iconReceived = false;
        alertMode = ALERT_ALLOWED;
        if (AppPrefs.toolbar_show_favicon.get()) {
            if (url.startsWith("yuzu:")) {
                removeIcon();
            } else {
                setIcon(loadingIcon);
                loadingIcon.start();
            }
        }
    }

    @Override
    public void onPageFinished(CustomWebView web, String url) {
        super.onPageFinished(web, url);
        setText((getTitle() != null) ? getTitle() : url);
        finished = true;
        if (bgTab) {
            bgTab = false;
//TODO: Restore this when Google fixes the bug where the WebView is blank after calling onPause followed by onResume.
//            if (AppPrefs.pause_web_tab_change.get())
//                mWebView.onPause();
        }
        if (AppPrefs.toolbar_show_favicon.get() && !url.startsWith("yuzu:") && !iconReceived) {
            setIcon(context.getDrawable(R.drawable.ic_page_white_24px));
        }
    }

    @Override
    public void onReceivedTitle(String title) {
        super.onReceivedTitle(title);
        setText(getTitle());
    }

    public void onReceivedIcon(Bitmap bitmap) {
        if (loadingIcon.isRunning()) {
            loadingIcon.stop();
        }
        iconReceived = true;
        if (AppPrefs.toolbar_show_favicon.get()) {
            setIcon(new BitmapDrawable(context.getResources(), bitmap));
        }
    }

    @Override
    public void onStateChanged(String title, String url, String originalUrl, int progress, Boolean isLoading, FaviconManager faviconManager) {
        super.onStateChanged(title, url, originalUrl, progress, isLoading, faviconManager);
        if (title != null)
            setText(title);
        else
            setText(url);

        if (originalUrl != null && AppPrefs.toolbar_show_favicon.get()) {
            if (originalUrl.startsWith("yuzu:")) {
                removeIcon();
            } else {
                setIcon(new BitmapDrawable(context.getResources(), faviconManager.get(originalUrl)));
            }
        }
    }

    public void onMoveTabToBackground(Resources res, Resources.Theme theme) {
        ThemeData themedata = ThemeData.getInstance();
        if (themedata != null && themedata.tabBackgroundNormal != null)
            mTabView.setBackground(themedata.tabBackgroundNormal);
        else
            mTabView.setBackgroundResource(R.drawable.tab_background_normal);

        if (isPinning())
            if (themedata != null && themedata.tabTextColorPin != 0)
                titleTextView.setTextColor(themedata.tabTextColorPin);
            else
                titleTextView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_pinning, theme));
        else if (isNavLock())
            if (themedata != null && themedata.tabTextColorLock != 0)
                titleTextView.setTextColor(themedata.tabTextColorLock);
            else
                titleTextView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_locked, theme));
        else if (themedata != null && themedata.tabTextColorNormal != 0)
            titleTextView.setTextColor(themedata.tabTextColorNormal);
        else
            titleTextView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_normal, theme));
    }

    public void onMoveTabToForeground(Resources res, Resources.Theme theme) {
        ThemeData themedata = ThemeData.getInstance();
        if (themedata != null && themedata.tabBackgroundSelect != null)
            mTabView.setBackground(themedata.tabBackgroundSelect);
        else if (themedata != null && themedata.tabAccentColor != 0) {
            LayerDrawable drawable = (LayerDrawable) res.getDrawable(R.drawable.tab_background_selected, theme);
            GradientDrawable accent = (GradientDrawable) drawable.findDrawableByLayerId(R.id.tabAccent);
            int px = (int) (res.getDisplayMetrics().density * 3 + 0.5f);
            accent.setStroke(px, themedata.tabAccentColor);
            mTabView.setBackground(drawable);
        } else
            mTabView.setBackgroundResource(R.drawable.tab_background_selected);

        if (themedata != null && themedata.progressColor != 0) {
            loadingIcon.setTint(themedata.progressColor);
        }

        if (isPinning())
            if (themedata != null && themedata.tabTextColorPin != 0)
                titleTextView.setTextColor(themedata.tabTextColorPin);
            else
                titleTextView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_pinning, theme));
        else if (isNavLock())
            if (themedata != null && themedata.tabTextColorLock != 0)
                titleTextView.setTextColor(themedata.tabTextColorLock);
            else
                titleTextView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_locked, theme));
        else if (themedata != null && themedata.tabTextColorSelect != 0)
            titleTextView.setTextColor(themedata.tabTextColorSelect);
        else
            titleTextView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_selected, theme));
    }

    public void invalidateView(boolean isCurrent, Resources res, Resources.Theme theme) {
        if (isCurrent)
            onMoveTabToForeground(res, theme);
        else
            onMoveTabToBackground(res, theme);
    }

    private void setText(String text) {
        titleTextView.setText(text);
    }

    private void setIcon(Drawable drawable) {
        int size = titleTextView.getHeight() - titleTextView.getPaddingTop() - titleTextView.getPaddingBottom();
        drawable.setBounds(0, 0, size, size);
        titleTextView.setCompoundDrawables(drawable, null, null, null);
    }

    private void removeIcon() {
        titleTextView.setCompoundDrawables(null, null, null, null);
    }

    private final View mTabView;
    private final TextView titleTextView;
    private final AnimatedVectorDrawable loadingIcon;
    private final Context context;
    private WebSettingResetAction resetAction;
    private boolean finished;
    private boolean bgTab;
    private boolean startDocument;
    private boolean iconReceived;
    private int alertMode;
    private int cookieMode = COOKIE_UNDEFINED;
    private int renderingMode = RENDERING_MODE_UNDEFINED;

    public WebSettingResetAction getResetAction() {
        return resetAction;
    }

    public void setResetAction(WebSettingResetAction resetAction) {
        this.resetAction = resetAction;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setUpBgTab() {
        this.bgTab = true;
        //mWebView.onResume();
    }

    public boolean isStartDocument() {
        return startDocument;
    }

    public void setStartDocument(boolean startDocument) {
        this.startDocument = startDocument;
    }

    public View getTabView() {
        return mTabView;
    }

    public boolean isEnableCookie() {
        if (cookieMode == COOKIE_UNDEFINED) {
            return !AppPrefs.private_mode.get() && AppPrefs.accept_cookie.get();
        } else {
            return cookieMode == COOKIE_ENABLE;
        }
    }

    public int getCookieMode() {
        return cookieMode;
    }

    public void setCookieMode(int mode) {
        cookieMode = mode;
    }

    public boolean isAlertAllowed() {
        return alertMode >= 0;
    }

    public int getAlertMode() {
        return alertMode;
    }

    public void setAlertMode(int alertMode) {
        this.alertMode = alertMode;
    }

    public int getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(int renderingMode) {
        this.renderingMode = renderingMode;
    }

    public void resetRenderingMode() {
        renderingMode = RENDERING_MODE_UNDEFINED;
    }

    public static final int COOKIE_ENABLE = 1;
    public static final int COOKIE_DISABLE = 0;
    public static final int COOKIE_UNDEFINED = -1;

    public static final int ALERT_ALLOWED = 0;
    public static final int ALERT_MULTIPULE = 1;
    public static final int ALERT_BLOCKED = -1;

    public static final int RENDERING_MODE_UNDEFINED = -1;
}
