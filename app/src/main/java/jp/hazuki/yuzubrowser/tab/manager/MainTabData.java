/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.tab.manager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.action.WebSettingResetAction;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.theme.ThemeData;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class MainTabData extends TabData {
    public MainTabData(CustomWebView web, View view) {
        super(web);
        mTabView = view;
    }

    public MainTabData(CustomWebView web, View view, TabIndexData data) {
        super(web, data);
        mTabView = view;
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        super.onPageStarted(url, favicon);
        setText(url);
        finished = false;
    }

    @Override
    public void onPageFinished(CustomWebView web, String url) {
        super.onPageFinished(web, url);
        setText((getTitle() != null) ? getTitle() : url);
        finished = true;
        if (bgTab) {
            bgTab = false;
            if (AppData.pause_web_tab_change.get())
                mWebView.onPause();
        }
    }

    @Override
    public void onReceivedTitle(String title) {
        super.onReceivedTitle(title);
        setText(getTitle());
    }

    @Override
    public void onStateChanged(TabData tabdata) {
        super.onStateChanged(tabdata);
        if (getTitle() != null)
            setText(getTitle());
        else
            setText(getUrl());
    }

    public void onMoveTabToBackground(Resources res, Resources.Theme theme) {
        ThemeData themedata = ThemeData.getInstance();
        if (themedata != null && themedata.tabBackgroundNormal != null)
            mTabView.setBackground(themedata.tabBackgroundNormal);
        else
            mTabView.setBackgroundResource(R.drawable.tab_background_normal);

        TextView textView = (TextView) mTabView.findViewById(R.id.textView);
        if (isPinning())
            if (themedata != null && themedata.tabTextColorPin != 0)
                textView.setTextColor(themedata.tabTextColorPin);
            else
                textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_pinning, theme));
        else if (isNavLock())
            if (themedata != null && themedata.tabTextColorLock != 0)
                textView.setTextColor(themedata.tabTextColorLock);
            else
                textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_locked, theme));
        else if (themedata != null && themedata.tabTextColorNormal != 0)
            textView.setTextColor(themedata.tabTextColorNormal);
        else
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_normal, theme));
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

        TextView textView = (TextView) mTabView.findViewById(R.id.textView);
        if (isPinning())
            if (themedata != null && themedata.tabTextColorPin != 0)
                textView.setTextColor(themedata.tabTextColorPin);
            else
                textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_pinning, theme));
        else if (isNavLock())
            if (themedata != null && themedata.tabTextColorLock != 0)
                textView.setTextColor(themedata.tabTextColorLock);
            else
                textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_locked, theme));
        else if (themedata != null && themedata.tabTextColorSelect != 0)
            textView.setTextColor(themedata.tabTextColorSelect);
        else
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_selected, theme));
    }

    public void invalidateView(boolean isCurrent, Resources res, Resources.Theme theme) {
        if (isCurrent)
            onMoveTabToForeground(res, theme);
        else
            onMoveTabToBackground(res, theme);
    }

    private void setText(String text) {
        ((TextView) mTabView.findViewById(R.id.textView)).setText(text);
    }

    private final View mTabView;
    private WebSettingResetAction resetAction;
    private boolean finished;
    private boolean bgTab;

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
        mWebView.onResume();
    }
}
