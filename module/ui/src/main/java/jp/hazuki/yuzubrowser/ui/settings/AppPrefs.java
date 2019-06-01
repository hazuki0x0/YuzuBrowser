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

package jp.hazuki.yuzubrowser.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.webkit.WebSettings;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.ui.settings.container.BooleanContainer;
import jp.hazuki.yuzubrowser.ui.settings.container.Containable;
import jp.hazuki.yuzubrowser.ui.settings.container.FloatContainer;
import jp.hazuki.yuzubrowser.ui.settings.container.FontSizeContainer;
import jp.hazuki.yuzubrowser.ui.settings.container.IntContainer;
import jp.hazuki.yuzubrowser.ui.settings.container.LongContainer;
import jp.hazuki.yuzubrowser.ui.settings.container.StringContainer;
import jp.hazuki.yuzubrowser.ui.settings.container.ToolbarContainer;
import jp.hazuki.yuzubrowser.ui.theme.ThemeData;

import static jp.hazuki.yuzubrowser.ui.ConstantsKt.BROWSER_LOAD_URL_TAB_CURRENT;
import static jp.hazuki.yuzubrowser.ui.ConstantsKt.BROWSER_LOAD_URL_TAB_NEW_RIGHT;

public class AppPrefs {
    public static final String PREFERENCE_NAME = "main_preference";
    private static SoftReference<ArrayList<Containable>> sPreferenceList;

    public static final IntContainer lastLaunchVersion = new IntContainer("_lastLaunchVersion", -1);
    public static final IntContainer lastLaunchPrefVersion = new IntContainer("_lastLaunchPrefVersion", -1);
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
    public static final IntContainer web_customview_oritentation = new IntContainer("web_customview_oritentation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    public static final IntContainer tab_action_sensitivity = new IntContainer("tab_action_sensitivity", 150);
    public static final BooleanContainer show_zoom_button = new BooleanContainer("show_zoom_button", false);
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
    public static final IntContainer newtab_link = new IntContainer("newtab_link", BROWSER_LOAD_URL_TAB_CURRENT);
    public static final IntContainer newtab_speeddial = new IntContainer("newtab_speeddial", BROWSER_LOAD_URL_TAB_CURRENT);
    public static final IntContainer newtab_blank = new IntContainer("newtab_blank", BROWSER_LOAD_URL_TAB_NEW_RIGHT);
    public static final StringContainer proxy_address = new StringContainer("proxy_address", "");
    public static final BooleanContainer proxy_set = new BooleanContainer("proxy_set", false);
    public static final BooleanContainer proxy_https_set = new BooleanContainer("proxy_https_set", false);
    public static final StringContainer proxy_https_address = new StringContainer("proxy_https_address", "");
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
    public static final BooleanContainer userjs_enable = new BooleanContainer("userjs_enable", false);
    public static final BooleanContainer webswipe_enable = new BooleanContainer("webswipe_enable", false);
    public static final IntContainer webswipe_sensitivity_speed = new IntContainer("webswipe_sensitivity_speed", 5);
    public static final IntContainer webswipe_sensitivity_distance = new IntContainer("webswipe_sensitivity_distance", 15);
    public static final IntContainer auto_tab_save_delay = new IntContainer("auto_tab_save_delay", 0);
    public static final IntContainer minimum_font = new IntContainer("minimum_font", 8);
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
    public static final BooleanContainer slow_rendering = new BooleanContainer("slow_rendering", false);
    public static final BooleanContainer webRtc = new BooleanContainer("webRtc", true);
    public static final BooleanContainer mining_protect = new BooleanContainer("mining_protect", true);
    public static final BooleanContainer fake_chrome = new BooleanContainer("fake_chrome", false);
    public static final BooleanContainer menu_icon = new BooleanContainer("menu_icon", true);
    public static final StringContainer language = new StringContainer("language", "");
    public static final BooleanContainer whiteBackground = new BooleanContainer("white_background", true);
    //UI
    public static final BooleanContainer fullscreen = new BooleanContainer("fullscreen", false);
    public static final IntContainer oritentation = new IntContainer("oritentation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    public static final IntContainer swipebtn_sensitivity = new IntContainer("swipebtn_sensitivity", 150);
    public static final BooleanContainer touch_scrollbar_fixed_toolbar = new BooleanContainer("touch_scrollbar_fixed_toolbar", false);
    //Bookmark
    public static final BooleanContainer bookmarkSimpleDisplay = new BooleanContainer("bookmark_simpleDisplay", false);
    public static final BooleanContainer openBookmarkNewTab = new BooleanContainer("open_bookmark_new_tab", true);
    public static final IntContainer newtabBookmark = new IntContainer("newtab_bookmark", BROWSER_LOAD_URL_TAB_CURRENT);
    public static final IntContainer openBookmarkIconAction = new IntContainer("open_bookmark_icon_action", 1);
    public static final BooleanContainer bookmarkBreadcrumbs = new BooleanContainer("bookmark_breadcrumbs", true);
    public static final BooleanContainer saveBookmarkFolder = new BooleanContainer("save_bookmark_folder", false);
    public static final LongContainer saveBookmarkFolderId = new LongContainer("save_bookmark_folder_id", -1L);
    public static final IntContainer fontSizeBookmark = new IntContainer("font_size_bookmark", -1);
    //History
    public static final IntContainer fontSizeHistory = new IntContainer("font_size_history", -1);
    public static final IntContainer newtabHistory = new IntContainer("newtab_history", BROWSER_LOAD_URL_TAB_CURRENT);
    public static final IntContainer history_max_day = new IntContainer("history_max_day", 0);
    public static final IntContainer history_max_count = new IntContainer("history_max_count", 0);
    //Search
    public static final IntContainer searchSuggestType = new IntContainer("search_suggest", 0);
    public static final BooleanContainer searchSuggestHistories = new BooleanContainer("search_suggest_histories", true);
    public static final BooleanContainer searchSuggestBookmarks = new BooleanContainer("search_suggest_bookmarks", true);
    public static final StringContainer search_url = new StringContainer("search_url", "http://www.google.com/m?q=%s");
    public static final BooleanContainer searchUrlShowIcon = new BooleanContainer("search_url_show_icon", true);
    public static final BooleanContainer searchUrlSaveSwitching = new BooleanContainer("search_url_save_switching", false);
    //WebView
    public static final IntContainer fast_back_cache_size = new IntContainer("fast_back_cache_size", 5);
    public static final BooleanContainer fast_back = new BooleanContainer("fast_back", false);
    //AdBlock
    public static final BooleanContainer isAbpIgnoreGenericElement = new BooleanContainer("abpIgnoreGenericElement", false);
    public static final BooleanContainer isAbpUseElementHide = new BooleanContainer("abpUseElementHide", true);


    public static boolean load(Context context) {
        SharedPreferences shared_preference = getPreference(context);
        for (Containable pref : getPreferenceList()) {
            pref.read(shared_preference);
        }
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
        SharedPreferences.Editor editor = getPreference(context).edit();
        for (Containable pref : prefs) {
            pref.write(editor);
        }
        return editor.commit();
    }

    public static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private static ArrayList<Containable> getPreferenceList() {
        ArrayList<Containable> list;

        if (sPreferenceList != null && (list = sPreferenceList.get()) != null)
            return list;

        list = new ArrayList<>();
        try {
            Field[] fields = AppPrefs.class.getDeclaredFields();
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
