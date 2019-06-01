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

package jp.hazuki.yuzubrowser.legacy.settings.data;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.squareup.moshi.Moshi;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpDatabase;
import jp.hazuki.yuzubrowser.adblock.service.AbpUpdateService;
import jp.hazuki.yuzubrowser.core.utility.extensions.ContextExtensionsKt;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;
import jp.hazuki.yuzubrowser.download.compatible.DownloadInfoDatabase;
import jp.hazuki.yuzubrowser.download.service.DownloadDatabase;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.action.ActionList;
import jp.hazuki.yuzubrowser.legacy.action.SingleAction;
import jp.hazuki.yuzubrowser.legacy.action.item.CustomMenuSingleAction;
import jp.hazuki.yuzubrowser.legacy.action.item.FinishSingleAction;
import jp.hazuki.yuzubrowser.legacy.action.manager.HardButtonActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.LongPressActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.MenuActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionArrayManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.TabActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.ToolbarActionManager;
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager;
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgentList;
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgentUpdaterKt;
import jp.hazuki.yuzubrowser.legacy.utils.converter.PatternUrlConverter;
import jp.hazuki.yuzubrowser.legacy.webencode.WebTextEncode;
import jp.hazuki.yuzubrowser.legacy.webencode.WebTextEncodeList;
import jp.hazuki.yuzubrowser.search.model.provider.SearchSuggestProviders;
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl;
import jp.hazuki.yuzubrowser.search.repository.SearchUrlManager;
import jp.hazuki.yuzubrowser.ui.BrowserApplication;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;
import jp.hazuki.yuzubrowser.ui.utils.PackageUtils;

public class AppData {
    private static final int PREF_VERSION = 1;

    private static final String TAG = "AppData";

    public static void settingInitialValue(Context context, SharedPreferences shared_preference, Moshi moshi, AbpDatabase abpDatabase) {
        boolean modified = false;
        int lastLaunch = AppPrefs.lastLaunchVersion.get();
        if (lastLaunch < 0) {
            Logger.d(TAG, "is first launch");


            if (!PackageUtils.isPermissionDerivedFromMyPackage(context,
                    ((BrowserApplication) context.getApplicationContext()).getPermissionAppSignature())) {
                throw new SecurityException("permission.myapp.signature is not derived from my package.");
            }

            SoftButtonActionManager softBtnManager = SoftButtonActionManager.getInstance(context);
            softBtnManager.btn_url_center.press.add(SingleAction.makeInstance(SingleAction.SHOW_SEARCHBOX));
            softBtnManager.save(context);

            SoftButtonActionArrayManager softBtnAryManager = SoftButtonActionArrayManager.getInstance(context);
            softBtnAryManager.btn_tab_right.add(SingleAction.makeInstance(SingleAction.NEW_TAB));
            softBtnAryManager.btn_url_left.add(SingleAction.makeInstance(SingleAction.ADD_BOOKMARK));
            softBtnAryManager.btn_url_right.add(SingleAction.makeInstance(SingleAction.WEB_RELOAD_STOP));
            softBtnAryManager.save(context);

            HardButtonActionManager hardBtnManager = HardButtonActionManager.getInstance(context);
            hardBtnManager.back_press.action.add(SingleAction.makeInstance(SingleAction.GO_BACK));
            hardBtnManager.search_press.action.add(SingleAction.makeInstance(SingleAction.FIND_ON_PAGE));
            hardBtnManager.save(context);

            ToolbarActionManager toolbarManager = ToolbarActionManager.getInstance(context);
            toolbarManager.custombar1.add(SingleAction.makeInstance(SingleAction.GO_BACK));
            toolbarManager.custombar1.add(SingleAction.makeInstance(SingleAction.GO_FORWARD));
            toolbarManager.custombar1.add(SingleAction.makeInstance(SingleAction.SHOW_BOOKMARK), SingleAction.makeInstance(SingleAction.SHOW_HISTORY));
            toolbarManager.custombar1.add(SingleAction.makeInstance(SingleAction.TAB_LIST));
            toolbarManager.custombar1.add(SingleAction.makeInstance(SingleAction.OPEN_OPTIONS_MENU));
            toolbarManager.save(context);

            TabActionManager tabManager = TabActionManager.getInstance(context);
            tabManager.tab_press.action.add(SingleAction.makeInstance(SingleAction.CLOSE_TAB));
            tabManager.save(context);

            MenuActionManager menuManager = MenuActionManager.getInstance(context);
            {
                ActionList list = menuManager.browser_activity.getList();
                list.add(SingleAction.makeInstance(SingleAction.GO_HOME));
                list.add(SingleAction.makeInstance(SingleAction.SHOW_BOOKMARK));
                list.add(SingleAction.makeInstance(SingleAction.SHOW_HISTORY));
                list.add(SingleAction.makeInstance(SingleAction.SHOW_DOWNLOADS));
                list.add(SingleAction.makeInstance(SingleAction.SHARE_WEB));
                list.add(SingleAction.makeInstance(SingleAction.FIND_ON_PAGE));
                list.add(SingleAction.makeInstance(SingleAction.READ_IT_LATER));
                list.add(SingleAction.makeInstance(SingleAction.READ_IT_LATER_LIST));
                list.add(SingleAction.makeInstance(SingleAction.READER_MODE));
                list.add(SingleAction.makeInstance(SingleAction.ALL_ACTION));
                list.add(SingleAction.makeInstance(SingleAction.SHOW_SETTINGS));
                list.add(new FinishSingleAction(SingleAction.FINISH, true, false));
                menuManager.save(context);
            }

            LongPressActionManager manager = LongPressActionManager.getInstance(context);
            {
                CustomMenuSingleAction action = (CustomMenuSingleAction) SingleAction.makeInstance(SingleAction.CUSTOM_MENU);
                ActionList list = action.getActionList();
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_LINK_TEXT));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_PAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_PAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_PATTERN_MATCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_BLACK_LIST));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_WHITE_LIST));
                manager.link.action.add(action);
            }
            {
                CustomMenuSingleAction action = (CustomMenuSingleAction) SingleAction.makeInstance(SingleAction.CUSTOM_MENU);
                ActionList list = action.getActionList();
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_PATTERN_MATCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_IMAGE_BLACK_LIST));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_IMAGE_WHITE_LIST));
                manager.image.action.add(action);
            }
            {
                CustomMenuSingleAction action = (CustomMenuSingleAction) SingleAction.makeInstance(SingleAction.CUSTOM_MENU);
                ActionList list = action.getActionList();
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_PAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_BLACK_LIST));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_WHITE_LIST));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_PATTERN_MATCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_IMAGE_BLACK_LIST));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_ADD_IMAGE_WHITE_LIST));
                manager.image_link.action.add(action);
            }
            manager.save(context);

            AppPrefs.toolbar_progress.size.set(4);
            AppPrefs.toolbar_progress.visibility.setHideWhenEndLoading(true);
            AppPrefs.toolbar_custom1.size.set(42);
            AppPrefs.toolbar_custom1.location.set(ToolbarManager.LOCATION_BOTTOM);
            AppPrefs.toolbar_tab.visibility.setVisible(false);

            UserAgentList uaList = new UserAgentList();
            UserAgentUpdaterKt.init(uaList);
            uaList.write(context, moshi);

            WebTextEncodeList encodes = new WebTextEncodeList();
            encodes.add(new WebTextEncode("ISO-8859-1"));
            encodes.add(new WebTextEncode("UTF-8"));
            encodes.add(new WebTextEncode("UTF-16"));
            encodes.add(new WebTextEncode("Shift_JIS"));
            encodes.add(new WebTextEncode("EUC-JP"));
            encodes.add(new WebTextEncode("ISO-2022-JP"));
            encodes.write(context, moshi);

            {
                SearchUrlManager urlManager = new SearchUrlManager(context, moshi);
                String[] urls = context.getResources().getStringArray(R.array.default_search_url);
                String[] titles = context.getResources().getStringArray(R.array.default_search_url_name);
                int[] colors = context.getResources().getIntArray(R.array.default_search_url_color);
                SearchSuggestProviders providers = new SearchSuggestProviders(0, 0, new ArrayList<>());

                for (int i = 0; i < urls.length; i++) {
                    providers.add(new SearchUrl(titles[i], urls[i], colors[i]));
                }

                urlManager.save(providers.toSettings());
                AppPrefs.search_url.set(providers.get(0).getUrl());
                AppPrefs.commit(context, AppPrefs.search_url);
            }

            AdBlockInitSupportKt.initAbpFilter(context, abpDatabase);
        }


        int prefVersionCode = AppPrefs.lastLaunchPrefVersion.get();

        if (lastLaunch >= 0 && (lastLaunch < 410010 || prefVersionCode < PREF_VERSION)) {
            //version up code
            if (lastLaunch < 410010) {

                if (lastLaunch < 300000) {
                    PatternUrlConverter converter = new PatternUrlConverter();
                    converter.convert(context);
                }

                if (lastLaunch <= 300100 && "NORMAL>".equals(AppPrefs.layout_algorithm.get())) {
                    AppPrefs.layout_algorithm.set("NORMAL");
                }

                if (lastLaunch <= 300103 && "SINGLE_COLUMN>".equals(AppPrefs.layout_algorithm.get())) {
                    AppPrefs.layout_algorithm.set("SINGLE_COLUMN");
                }

                if (lastLaunch < 300200) {
                    SearchUrlManager urlManager = new SearchUrlManager(context, moshi);
                    String[] urls = context.getResources().getStringArray(R.array.default_search_url);
                    String[] titles = context.getResources().getStringArray(R.array.default_search_url_name);
                    int[] colors = context.getResources().getIntArray(R.array.default_search_url_color);
                    SearchSuggestProviders providers = new SearchSuggestProviders(urlManager.load());

                    for (int i = 0; i < urls.length; i++) {
                        providers.add(new SearchUrl(titles[i], urls[i], colors[i]));
                    }

                    if (!"http://www.google.com/m?q=%s".equals(AppPrefs.search_url.get())) {
                        providers.add(new SearchUrl("Custom", AppPrefs.search_url.get(), 0));
                        providers.setSelectedId(providers.get(providers.getSize() - 1).getId());
                    }

                    urlManager.save(providers.toSettings());
                    AppPrefs.search_url.set(providers.get(providers.getSelectedIndex()).getUrl());

                    AppPrefs.fullscreen_hide_mode.set(shared_preference.getBoolean("fullscreen_hide_nav", false) ? 2 : 0);

                    shared_preference.edit().remove("fullscreen_hide_nav").apply();
                }

                if (lastLaunch < 300303 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager manager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.deleteNotificationChannel("jp.hazuki.yuzubrowser.channel.dl.notify");
                }

                if (lastLaunch < 400100) {
                    DownloadInfoDatabase from = new DownloadInfoDatabase(context);
                    DownloadDatabase to = DownloadDatabase.Companion.getInstance(context);

                    to.convert(from.getConvertData());

                    from.deleteDatabase(context);
                    AppPrefs.download_folder.set("file://" + AppPrefs.download_folder.get());
                }

                if (lastLaunch < 410010) {
                    AdBlockInitSupportKt.initAbpFilter(context, abpDatabase);
                    AdBlockInitSupportKt.disableYuzuList(abpDatabase);
                }
            }
            if (lastLaunch == 410010) {
                AbpUpdateService.Companion.updateAll(context, true, null);
            }

            AppPrefs.lastLaunchPrefVersion.set(PREF_VERSION);
            modified = true;
        }

        int versionCode = ContextExtensionsKt.getVersionCode(context);

        if (versionCode > lastLaunch) {
            if (lastLaunch > -1) {
                UserAgentList list = new UserAgentList();
                list.read(context, moshi);
                UserAgentUpdaterKt.upgrade(list);
                list.write(context, moshi);
            }

            AppPrefs.lastLaunchVersion.set(versionCode);
            modified = true;
        }

        if (modified) AppPrefs.commit(context);
    }

    public static boolean init(Context context, Moshi moshi, AbpDatabase abpDatabase) {
        AppPrefs.load(context);
        settingInitialValue(context, AppPrefs.getPreference(context), moshi, abpDatabase);
        return true;
    }
}
