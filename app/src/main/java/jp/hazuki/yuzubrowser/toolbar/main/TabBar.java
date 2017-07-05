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

package jp.hazuki.yuzubrowser.toolbar.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionArrayManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.theme.ThemeData;
import jp.hazuki.yuzubrowser.toolbar.ButtonToolbarController;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager.RequestCallback;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.view.tab.FullTabLayout;
import jp.hazuki.yuzubrowser.utils.view.tab.ScrollableTabLayout;
import jp.hazuki.yuzubrowser.utils.view.tab.TabLayout;

public class TabBar extends ToolbarBase {
    public static final int TAB_TYPE_SCROLLABLE = 0;
    public static final int TAB_TYPE_FULL = 1;
    private final int TAB_SIZE_X, TAB_SIZE_Y, TAB_FONT_SIZE;
    private final TabLayout mTabLayout;
    private final ButtonToolbarController mLeftButtonController;
    private final ButtonToolbarController mRightButtonController;

    public TabBar(Context context, final ActionCallback action_callback, RequestCallback request_callback) {
        super(context, AppData.toolbar_tab, R.layout.toolbar_tab, request_callback);

        TAB_SIZE_X = DisplayUtils.convertDpToPx(context, AppData.tab_size_x.get());
        TAB_SIZE_Y = DisplayUtils.convertDpToPx(context, AppData.toolbar_tab.size.get());
        TAB_FONT_SIZE = AppData.tab_font_size.get();

        mLeftButtonController = new ButtonToolbarController((LinearLayout) findViewById(R.id.leftLinearLayout), action_callback, TAB_SIZE_Y);
        mRightButtonController = new ButtonToolbarController((LinearLayout) findViewById(R.id.rightLinearLayout), action_callback, TAB_SIZE_Y);

        LinearLayout tabLayoutBase = (LinearLayout) findViewById(R.id.tabLayoutBase);
        switch (AppData.tab_type.get()) {
            case TAB_TYPE_SCROLLABLE: {
                ScrollableTabLayout tab_layout = new ScrollableTabLayout(context);
                tabLayoutBase.addView(tab_layout);
                mTabLayout = tab_layout;
            }
            break;
            case TAB_TYPE_FULL: {
                FullTabLayout tab_layout = new FullTabLayout(context);
                tabLayoutBase.addView(tab_layout);
                mTabLayout = tab_layout;
            }
            break;
            default:
                throw new IllegalArgumentException();
        }

        addButtons();
    }

    @Override
    public void notifyChangeWebState(MainTabData data) {
        super.notifyChangeWebState(data);
        mLeftButtonController.notifyChangeState();
        mRightButtonController.notifyChangeState();
    }

    @Override
    public void resetToolBar() {
        mLeftButtonController.resetIcon();
        mRightButtonController.resetIcon();
    }

    @Override
    public void applyTheme(ThemeData themedata) {
        super.applyTheme(themedata);
        applyTheme(themedata, mLeftButtonController);
        applyTheme(themedata, mRightButtonController);
        mTabLayout.applyTheme(themedata);
    }

    private void addButtons() {
        SoftButtonActionArrayManager manager = SoftButtonActionArrayManager.getInstance(getContext());
        mLeftButtonController.addButtons(manager.btn_tab_left.list);
        mRightButtonController.addButtons(manager.btn_tab_right.list);
        onThemeChanged(ThemeData.getInstance());// TODO
    }

    public View addNewTabView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tab_item, null, true);
        ((TextView) view.findViewById(R.id.textView)).setTextSize(TAB_FONT_SIZE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(TAB_SIZE_X, TAB_SIZE_Y);
        mTabLayout.addTabView(view, params);
        return view;
    }

    @Override
    public void onPreferenceReset() {
        super.onPreferenceReset();
        addButtons();

        mTabLayout.onPreferenceReset();
        mTabLayout.setSense(AppData.tab_action_sensitivity.get());
    }

    public void removeTab(int no) {
        mTabLayout.removeTabAt(no);
    }

    public void setOnTabClickListener(TabLayout.OnTabClickListener l) {
        mTabLayout.setOnTabClickListener(l);
    }

    public void changeCurrentTab(int to) {
        mTabLayout.setCurrentTab(to);
    }

    public void fullScrollRight() {
        mTabLayout.fullScrollRight();
    }

    public void fullScrollLeft() {
        mTabLayout.fullScrollLeft();
    }

    public void scrollToPosition(int position) {
        mTabLayout.scrollToPosition(position);
    }

    public void swapTab(int a, int b) {
        mTabLayout.swapTab(a, b);
    }

    public void moveTab(int from, int to, int new_curernt) {
        mTabLayout.moveTab(from, to, new_curernt);
    }
}
