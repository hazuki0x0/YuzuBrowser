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

package jp.hazuki.yuzubrowser.legacy.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.adblock.filter.fastmatch.FastMatcherFactory;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.utils.PackageUtils;

public class WebUtils {
    private WebUtils() {
        throw new UnsupportedOperationException();
    }

    private static final Pattern URL_EXTRACTION = Pattern.compile("((?:http|https|file|market)://|(?:inline|data|about|content|javascript|mailto|view-source|yuzu|blob):)(\\S*)", Pattern.CASE_INSENSITIVE);

    private static final Pattern URL_SUB_DOMAIN = Pattern.compile("://.*\\.", Pattern.LITERAL);

    public static String extractionUrl(String text) {
        if (text == null) return null;
        Matcher matcher = URL_EXTRACTION.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return text;
        }
    }

    public static boolean isOverrideScheme(Uri uri) {
        switch (uri.getScheme().toLowerCase()) {
            case "http":
            case "https":
            case "file":
            case "inline":
            case "data":
            case "about":
            case "content":
            case "javascript":
            case "view-source":
            case "blob":
                return false;
            default:
                return true;
        }
    }

    public static String makeSearchUrlFromQuery(String query, String search_url, String search_place_holder) {
        return URLUtil.composeSearchUrl(query.trim(), search_url, search_place_holder);
    }

    public static Intent createShareWebIntent(String url, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        if (title != null)
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
        return intent;
    }

    public static void shareWeb(Context context, String url, String title) {
        if (url == null) return;

        Intent intent = createShareWebIntent(url, title);
        try {
            context.startActivity(Intent.createChooser(intent, context.getText(R.string.share)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Intent createOpenInOtherAppIntent(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent createOpenInOtherAppIntent(Intent intent, String url) {
        intent.setData(Uri.parse(url));
        return intent;
    }

    public static void openInOtherApp(Context context, String url) {
        if (url == null) return;
        try {
            context.startActivity(PackageUtils.createChooser(context, url, context.getText(R.string.open_other_app)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Pattern makeUrlPatternWithThrow(FastMatcherFactory factory, String pattern_url) {
        if (pattern_url == null || pattern_url.length() == 0) return null;
        if (pattern_url.charAt(0) == '[' && pattern_url.charAt(pattern_url.length() - 1) == ']') {
            return Pattern.compile(pattern_url.substring(1, pattern_url.length() - 1));
        } else {
            pattern_url = factory.fastCompile(pattern_url);
            if (pattern_url.startsWith(".*\\.")) {
                pattern_url = "((?![./]).)*" + pattern_url.substring(3);
            } else if (pattern_url.contains("://.*\\.")) {
                pattern_url = URL_SUB_DOMAIN.matcher(pattern_url).replaceFirst("://((?![\\./]).)*\\.");
            }
            if (pattern_url.startsWith("http.*://") && pattern_url.length() >= 10) {
                pattern_url = "https?://" + pattern_url.substring(9);
            }
            if (maybeContainsUrlScheme(pattern_url))
                return Pattern.compile("^" + pattern_url);
            else
                return Pattern.compile("^\\w+://" + pattern_url);
        }
    }

    private static final Pattern sSchemeContainsPattern = Pattern.compile("^\\w+:", Pattern.CASE_INSENSITIVE);

    public static boolean maybeContainsUrlScheme(String url) {
        return url != null && sSchemeContainsPattern.matcher(url).find();
    }
}
