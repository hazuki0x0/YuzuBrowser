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
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Environment;
import android.webkit.WebSettings;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.core.utility.extensions.ContextExtensionsKt;
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;
import jp.hazuki.yuzubrowser.legacy.BrowserApplication;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.action.ActionList;
import jp.hazuki.yuzubrowser.legacy.action.SingleAction;
import jp.hazuki.yuzubrowser.legacy.action.item.CustomMenuSingleAction;
import jp.hazuki.yuzubrowser.legacy.action.manager.HardButtonActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.LongPressActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.MenuActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionArrayManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.TabActionManager;
import jp.hazuki.yuzubrowser.legacy.action.manager.ToolbarActionManager;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.legacy.browser.BrowserManager;
import jp.hazuki.yuzubrowser.legacy.download.compatible.DownloadInfoDatabase;
import jp.hazuki.yuzubrowser.legacy.download.service.DownloadDatabase;
import jp.hazuki.yuzubrowser.legacy.search.settings.SearchUrl;
import jp.hazuki.yuzubrowser.legacy.search.settings.SearchUrlManager;
import jp.hazuki.yuzubrowser.legacy.settings.PreferenceConstants;
import jp.hazuki.yuzubrowser.legacy.settings.container.BooleanContainer;
import jp.hazuki.yuzubrowser.legacy.settings.container.Containable;
import jp.hazuki.yuzubrowser.legacy.settings.container.FloatContainer;
import jp.hazuki.yuzubrowser.legacy.settings.container.IntContainer;
import jp.hazuki.yuzubrowser.legacy.settings.container.LongContainer;
import jp.hazuki.yuzubrowser.legacy.settings.container.StringContainer;
import jp.hazuki.yuzubrowser.legacy.settings.container.ToolbarContainer;
import jp.hazuki.yuzubrowser.legacy.settings.container.custom.FontSizeContainer;
import jp.hazuki.yuzubrowser.legacy.theme.ThemeData;
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager;
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgent;
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgentList;
import jp.hazuki.yuzubrowser.legacy.utils.PackageUtils;
import jp.hazuki.yuzubrowser.legacy.utils.converter.PatternUrlConverter;
import jp.hazuki.yuzubrowser.legacy.webencode.WebTextEncode;
import jp.hazuki.yuzubrowser.legacy.webencode.WebTextEncodeList;

public class AppData {
    private static final String TAG = "AppData";
    public static final String PREFERENCE_NAME = "main_preference";
    private static SoftReference<ArrayList<Containable>> sPreferenceList;

    public static final IntContainer lastLaunchVersion = new IntContainer("_lastLaunchVersion", -1);
    public static final IntContainer clear_data_default = new IntContainer("_clear_data_default", 0);
    public static final IntContainer finish_alert_default = new IntContainer("_finish_alert_default", 0);

    public static final IntContainer tab_type = new IntContainer("tab_type", 0);
    public static final IntContainer tab_size_x = new IntContainer("tab_size_x", 150);
    public static final IntContainer tab_font_size = new IntContainer("tab_font_size", 14);
    public static final IntContainer toolbar_text_size_url = new IntContainer("toolbar_text_size_url", 14);
    public static final ToolbarContainer toolbar_tab = new ToolbarContainer("tab", 1);
    public static final ToolbarContainer toolbar_url = new ToolbarContainer("url", 1);
    public static final BooleanContainer toolbar_url_box = new BooleanContainer("toolbar_url_box", true);
    public static final ToolbarContainer toolbar_progress = new ToolbarContainer("progress", 1);
    public static final ToolbarContainer toolbar_custom1 = new ToolbarContainer("custom1", 1);
    public static final BooleanContainer toolbar_always_show_url = new BooleanContainer("toolbar_always_show_url", true);
    public static final BooleanContainer toolbar_show_favicon = new BooleanContainer("toolbar_show_favicon", true);
    public static final BooleanContainer toolbar_auto_open = new BooleanContainer("toolbar_auto_open", true);
    public static final BooleanContainer toolbar_small_icon = new BooleanContainer("toolbar_small_icon", true);
    public static final IntContainer swipebtn_sensitivity = new IntContainer("swipebtn_sensitivity", 150);
    public static final StringContainer default_encoding = new StringContainer("default_encoding", "UTF-8");
    public static final StringContainer user_agent = new StringContainer("user_agent", "");
    public static final IntContainer text_size = new IntContainer("text_size", 100);
    public static final BooleanContainer private_mode = new BooleanContainer("private_mode", false);
    public static final BooleanContainer javascript = new BooleanContainer("javascript", true);
    public static final BooleanContainer web_db = new BooleanContainer("web_db", true);
    public static final BooleanContainer web_dom_db = new BooleanContainer("web_dom_db", true);
    public static final StringContainer home_page = new StringContainer("home_page", "yuzu:speeddial");
    public static final BooleanContainer load_overview = new BooleanContainer("load_overview", true);
    public static final BooleanContainer web_wideview = new BooleanContainer("web_wideview", true);
    public static final IntContainer oritentation = new IntContainer("oritentation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    public static final IntContainer web_customview_oritentation = new IntContainer("web_customview_oritentation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    public static final StringContainer search_url = new StringContainer("search_url", "http://www.google.com/m?q=%s");
    public static final IntContainer tab_action_sensitivity = new IntContainer("tab_action_sensitivity", 150);
    public static final BooleanContainer show_zoom_button = new BooleanContainer("show_zoom_button", false);
    public static final BooleanContainer fullscreen = new BooleanContainer("fullscreen", false);
    public static final BooleanContainer keep_screen_on = new BooleanContainer("keep_screen_on", false);
    public static final BooleanContainer accept_cookie = new BooleanContainer("accept_cookie", true);
    public static final BooleanContainer accept_third_cookie = new BooleanContainer("accept_third_cookie", true);
    public static final BooleanContainer accept_cookie_private = new BooleanContainer("accept_cookie_private", false);
    public static final IntContainer web_cache = new IntContainer("web_cache", WebSettings.LOAD_DEFAULT);
    public static final BooleanContainer web_popup = new BooleanContainer("web_popup", true);
    public static final IntContainer download_action = new IntContainer("download_action", PreferenceConstants.DOWNLOAD_AUTO);
    public static final StringContainer download_folder = new StringContainer("download_folder", "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    public static final BooleanContainer pause_web_background = new BooleanContainer("pause_web_background", true);
    public static final BooleanContainer save_formdata = new BooleanContainer("save_formdata", true);
    //TODO: Restore this when Google fixes the bug where the WebView is blank after calling onPause followed by onResume.
    //public static final BooleanContainer pause_web_tab_change = new BooleanContainer("pause_web_tab_change", true);
    public static final BooleanContainer web_app_cache = new BooleanContainer("web_app_cache", true);
    public static final BooleanContainer web_geolocation = new BooleanContainer("web_geolocation", true);
    public static final StringContainer layout_algorithm = new StringContainer("layout_algorithm", "NORMAL");
    public static final BooleanContainer save_history = new BooleanContainer("save_history", true);
    public static final IntContainer newtab_link = new IntContainer("newtab_link", BrowserManager.LOAD_URL_TAB_CURRENT);
    public static final IntContainer newtab_speeddial = new IntContainer("newtab_speeddial", BrowserManager.LOAD_URL_TAB_CURRENT);
    public static final IntContainer newtab_bookmark = new IntContainer("newtab_bookmark", BrowserManager.LOAD_URL_TAB_CURRENT);
    public static final IntContainer newtab_history = new IntContainer("newtab_history", BrowserManager.LOAD_URL_TAB_CURRENT);
    public static final IntContainer newtab_blank = new IntContainer("newtab_blank", BrowserManager.LOAD_URL_TAB_NEW_RIGHT);
    public static final StringContainer proxy_address = new StringContainer("proxy_address", "");
    public static final BooleanContainer proxy_set = new BooleanContainer("proxy_set", false);
    public static final BooleanContainer ssl_error_alert = new BooleanContainer("ssl_error_alert", true);
    public static final BooleanContainer gesture_enable_web = new BooleanContainer("gesture_enable_web", false);
    public static final BooleanContainer gesture_line_web = new BooleanContainer("gesture_line_web", true);
    public static final FloatContainer gesture_score_web = new FloatContainer("gesture_score_web", 4.0F);
    public static final FloatContainer gesture_score_sub = new FloatContainer("gesture_score_sub", 4.0F);
    public static final IntContainer file_access = new IntContainer("file_access", PreferenceConstants.FILE_ACCESS_SAFER);
    public static final BooleanContainer flick_enable = new BooleanContainer("flick_enable", false);
    public static final IntContainer flick_sensitivity_speed = new IntContainer("flick_sensitivity_speed", 20);
    public static final IntContainer flick_sensitivity_distance = new IntContainer("flick_sensitivity_distance", 15);
    public static final BooleanContainer flick_edge = new BooleanContainer("flick_edge", true);
    public static final BooleanContainer flick_disable_scroll = new BooleanContainer("flick_disable_scroll", true);
    public static final BooleanContainer qc_enable = new BooleanContainer("qc_enable", false);
    public static final IntContainer qc_rad_start = new IntContainer("qc_rad_start", 50);
    public static final IntContainer qc_rad_inc = new IntContainer("qc_rad_inc", 70);
    public static final IntContainer qc_slop = new IntContainer("qc_slop", 10);
    public static final IntContainer qc_position = new IntContainer("qc_position", 0);
    public static final IntContainer overlay_bottom_alpha = new IntContainer("overlay_bottom_alpha", 0xee);
    public static final BooleanContainer save_last_tabs = new BooleanContainer("save_last_tabs", false);
    public static final BooleanContainer save_closed_tab = new BooleanContainer("save_closed_tab", false);
    public static final BooleanContainer fast_back = new BooleanContainer("fast_back", false);
    public static final IntContainer fast_back_cache_size = new IntContainer("fast_back_cache_size", 5);
    public static final BooleanContainer userjs_enable = new BooleanContainer("userjs_enable", false);
    public static final BooleanContainer webswipe_enable = new BooleanContainer("webswipe_enable", false);
    public static final IntContainer webswipe_sensitivity_speed = new IntContainer("webswipe_sensitivity_speed", 5);
    public static final IntContainer webswipe_sensitivity_distance = new IntContainer("webswipe_sensitivity_distance", 15);
    public static final IntContainer auto_tab_save_delay = new IntContainer("auto_tab_save_delay", 0);
    public static final IntContainer minimum_font = new IntContainer("minimum_font", 8);
    public static final BooleanContainer kill_process = new BooleanContainer("kill_process", false);
    public static final IntContainer search_suggest = new IntContainer("search_suggest", 0);
    public static final BooleanContainer search_suggest_histories = new BooleanContainer("search_suggest_histories", true);
    public static final BooleanContainer search_suggest_bookmarks = new BooleanContainer("search_suggest_bookmarks", true);
    public static final IntContainer history_max_day = new IntContainer("history_max_day", 0);
    public static final IntContainer history_max_count = new IntContainer("history_max_count", 0);
    public static final BooleanContainer detailed_log = new BooleanContainer("detailed_log", false);
    public static final StringContainer theme_setting = new StringContainer("theme_setting", ThemeData.THEME_LIGHT);
    public static final BooleanContainer resblock_enable = new BooleanContainer("resblock_enable", false);
    public static final BooleanContainer allow_content_access = new BooleanContainer("allow_content_access", true);
    public static final BooleanContainer pull_to_refresh = new BooleanContainer("pull_to_refresh", true);
    public static final BooleanContainer share_unknown_scheme = new BooleanContainer("share_unknown_scheme", true);
    public static final BooleanContainer double_tap_flick_enable = new BooleanContainer("double_tap_flick_enable", false);
    public static final IntContainer double_tap_flick_sensitivity_speed = new IntContainer("double_tap_flick_sensitivity_speed", 20);
    public static final IntContainer double_tap_flick_sensitivity_distance = new IntContainer("double_tap_flick_sensitivity_distance", 15);
    public static final IntContainer mixed_content = new IntContainer("mixed_content", WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
    public static final BooleanContainer save_bookmark_folder = new BooleanContainer("save_bookmark_folder", false);
    public static final LongContainer save_bookmark_folder_id = new LongContainer("save_bookmark_folder_id", -1L);
    public static final BooleanContainer open_bookmark_new_tab = new BooleanContainer("open_bookmark_new_tab", true);
    public static final IntContainer open_bookmark_icon_action = new IntContainer("open_bookmark_icon_action", 1);
    public static final IntContainer search_suggest_engine = new IntContainer("search_suggest_engine", 0);
    public static final IntContainer tabs_cache_number = new IntContainer("tab_cache_number", 5);
    public static final IntContainer rendering = new IntContainer("rendering", 0);
    public static final BooleanContainer move_to_parent = new BooleanContainer("move_to_parent", true);
    public static final BooleanContainer move_to_left_tab = new BooleanContainer("move_to_left_tab", false);
    public static final BooleanContainer multi_finger_gesture = new BooleanContainer("multi_finger_gesture", false);
    public static final BooleanContainer multi_finger_gesture_show_name = new BooleanContainer("multi_finger_gesture_show_name", false);
    public static final IntContainer multi_finger_gesture_sensitivity = new IntContainer("multi_finger_gesture_sensitivity", 30);
    public static final IntContainer menu_btn_list_mode = new IntContainer("menu_btn_list_mode", 1);
    public static final BooleanContainer block_web_images = new BooleanContainer("block_web_images", false);
    public static final BooleanContainer save_pinned_tabs = new BooleanContainer("save_pinned_tabs", true);
    public static final BooleanContainer ad_block = new BooleanContainer("ad_block", false);
    public static final IntContainer night_mode_color = new IntContainer("night_mode_color", 5000);
    public static final IntContainer night_mode_bright = new IntContainer("night_mode_bright", 100);
    public static final IntContainer show_tab_divider = new IntContainer("show_tab_divider", 0);
    public static final BooleanContainer volume_default_playing = new BooleanContainer("volume_default_playing", true);
    public static final BooleanContainer snap_toolbar = new BooleanContainer("snap_toolbar", true);
    public static final IntContainer fullscreen_hide_mode = new IntContainer("fullscreen_hide_mode", 0);
    public static final BooleanContainer speeddial_show_header = new BooleanContainer("speeddial_show_header", true);
    public static final BooleanContainer speeddial_show_search = new BooleanContainer("speeddial_show_search", true);
    public static final BooleanContainer speeddial_show_icon = new BooleanContainer("speeddial_show_icon", true);
    public static final IntContainer speeddial_column = new IntContainer("speeddial_column", 4);
    public static final IntContainer speeddial_column_landscape = new IntContainer("speeddial_column_landscape", 5);
    public static final IntContainer speeddial_column_width = new IntContainer("speeddial_column_width", 80);
    public static final BooleanContainer speeddial_dark_theme = new BooleanContainer("speeddial_dark_theme", false);
    public static final IntContainer speeddial_layout = new IntContainer("speeddial_layout", 0);
    public static final BooleanContainer safe_browsing = new BooleanContainer("safe_browsing", true);
    public static final BooleanContainer save_tabs_for_crash = new BooleanContainer("save_tabs_for_crash", false);
    public static final IntContainer touch_scrollbar = new IntContainer("touch_scrollbar", -1);
    public static final IntContainer reader_theme = new IntContainer("reader_theme", -1);
    public static final IntContainer reader_text_size = new IntContainer("reader_text_size", 18);
    public static final StringContainer reader_text_font = new StringContainer("reader_text_font", "");
    public static final FontSizeContainer font_size = new FontSizeContainer();
    public static final BooleanContainer search_url_show_icon = new BooleanContainer("search_url_show_icon", true);
    public static final BooleanContainer slow_rendering = new BooleanContainer("slow_rendering", false);
    public static final BooleanContainer touch_scrollbar_fixed_toolbar = new BooleanContainer("touch_scrollbar_fixed_toolbar", false);
    public static final BooleanContainer webRtc = new BooleanContainer("webRtc", true);
    public static final BooleanContainer mining_protect = new BooleanContainer("mining_protect", true);
    public static final BooleanContainer fake_chrome = new BooleanContainer("fake_chrome", false);
    public static final BooleanContainer bookmark_breadcrumbs = new BooleanContainer("bookmark_breadcrumbs", true);
    public static final BooleanContainer menu_icon = new BooleanContainer("menu_icon", true);
    public static final StringContainer language = new StringContainer("language", "");


    public static void settingInitialValue(Context context, SharedPreferences shared_preference) {
        int lastLaunch = lastLaunchVersion.get();
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
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.GO_HOME));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.SHOW_BOOKMARK));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.SHOW_HISTORY));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.SHOW_DOWNLOADS));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.SHARE_WEB));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.FIND_ON_PAGE));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.READ_IT_LATER));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.READ_IT_LATER_LIST));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.READER_MODE));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.ALL_ACTION));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.SHOW_SETTINGS));
            menuManager.browser_activity.getList().add(SingleAction.makeInstance(SingleAction.FINISH));
            menuManager.save(context);

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

            toolbar_progress.size.set(4);
            toolbar_progress.visibility.setHideWhenEndLoading(true);
            toolbar_custom1.size.set(42);
            toolbar_custom1.location.set(ToolbarManager.LOCATION_BOTTOM);
            toolbar_tab.visibility.setVisible(false);

            UserAgentList uaList = new UserAgentList();
            uaList.add(new UserAgent("android", "Mozilla/5.0 (Linux; Android 8.1.0; Nexus 5X Build/OPM1.171019.011) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Mobile Safari/537.36"));
            uaList.add(new UserAgent("android Tablet", "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 9 Build/N4F26M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Safari/537.36"));
            uaList.add(new UserAgent("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1"));
            uaList.add(new UserAgent("iPad", "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1"));
            uaList.add(new UserAgent("PC", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"));
            uaList.write(context);

            WebTextEncodeList encodes = new WebTextEncodeList();
            encodes.add(new WebTextEncode("ISO-8859-1"));
            encodes.add(new WebTextEncode("UTF-8"));
            encodes.add(new WebTextEncode("UTF-16"));
            encodes.add(new WebTextEncode("Shift_JIS"));
            encodes.add(new WebTextEncode("EUC-JP"));
            encodes.add(new WebTextEncode("ISO-2022-JP"));
            encodes.write(context);

            {
                SearchUrlManager urlManager = new SearchUrlManager(context);
                String[] urls = context.getResources().getStringArray(R.array.default_search_url);
                String[] titles = context.getResources().getStringArray(R.array.default_search_url_name);
                int[] colors = context.getResources().getIntArray(R.array.default_search_url_color);

                for (int i = 0; i < urls.length; i++) {
                    urlManager.add(new SearchUrl(titles[i], urls[i], colors[i]));
                }

                urlManager.save();
                AppData.search_url.set(urlManager.get(0).getUrl());
                AppData.commit(context, AppData.search_url);
            }
        }


        int versionCode = ContextExtensionsKt.getVersionCode(context);

        if (lastLaunch == 1130000) {
            lastLaunch = 0;
        }

        if (lastLaunch < versionCode) {
            if (lastLaunch >= 0) {
                //version up code
                if (lastLaunch < 106000) {
                    BookmarkManager manager = BookmarkManager.getInstance(context);
                    manager.save();
                }

                if (lastLaunch < 200000) {
                    //TODO : add old app converter class
                    //BundleDataBaseConverter converter = new BundleDataBaseConverter(context.getFileStreamPath("last_url_2.dat"));
                    //converter.readList(context);
                    //converter.clear();
                }

                if (lastLaunch < 300000) {
                    PatternUrlConverter converter = new PatternUrlConverter();
                    converter.convert(context);
                }

                if (lastLaunch <= 300100 && "NORMAL>".equals(AppData.layout_algorithm.get())) {
                    AppData.layout_algorithm.set("NORMAL");
                }

                if (lastLaunch <= 300103 && "SINGLE_COLUMN>".equals(AppData.layout_algorithm.get())) {
                    AppData.layout_algorithm.set("SINGLE_COLUMN");
                }

                if (lastLaunch < 300200) {
                    SearchUrlManager urlManager = new SearchUrlManager(context);
                    String[] urls = context.getResources().getStringArray(R.array.default_search_url);
                    String[] titles = context.getResources().getStringArray(R.array.default_search_url_name);
                    int[] colors = context.getResources().getIntArray(R.array.default_search_url_color);

                    for (int i = 0; i < urls.length; i++) {
                        urlManager.add(new SearchUrl(titles[i], urls[i], colors[i]));
                    }

                    if (!"http://www.google.com/m?q=%s".equals(AppData.search_url.get())) {
                        urlManager.add(new SearchUrl("Custom", AppData.search_url.get(), 0));
                        urlManager.setSelectedId(urlManager.get(urlManager.size() - 1).getId());
                    }

                    urlManager.save();
                    AppData.search_url.set(urlManager.get(urlManager.getSelectedIndex()).getUrl());

                    AppData.fullscreen_hide_mode.set(shared_preference.getBoolean("fullscreen_hide_nav", false) ? 2 : 0);

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
                    download_folder.set("file://" + download_folder.get());
                }
            }

            lastLaunchVersion.set(versionCode);
            commit(context);
        }
    }

    public static boolean load(Context context) {
        SharedPreferences shared_preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        for (Containable pref : getPreferenceList()) {
            pref.read(shared_preference);
        }
        settingInitialValue(context, shared_preference);
        return true;
    }

    public static boolean commit(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        for (Containable pref : getPreferenceList()) {
            pref.write(editor);
        }
        return editor.commit();
    }

    public static boolean commit(Context context, Containable... prefs) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        for (Containable pref : prefs) {
            pref.write(editor);
        }
        return editor.commit();
    }

    private static ArrayList<Containable> getPreferenceList() {
        ArrayList<Containable> list;

        if (sPreferenceList != null && (list = sPreferenceList.get()) != null)
            return list;

        list = new ArrayList<>();
        try {
            Field[] fields = AppData.class.getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(null);
                if (obj instanceof Containable) {// null returns false
                    list.add((Containable) obj);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }

        sPreferenceList = new SoftReference<>(list);

        return list;
    }
}
