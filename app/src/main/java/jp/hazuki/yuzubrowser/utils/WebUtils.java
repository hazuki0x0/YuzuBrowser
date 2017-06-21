package jp.hazuki.yuzubrowser.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.R;

public class WebUtils {
    private WebUtils() {
        throw new UnsupportedOperationException();
    }

    private static final Pattern URI_SCHEMA = Pattern.compile("((?:http|https|file|market)://|(?:inline|data|about|content|javascript|mailto|view-source|yuzu):)(.*)", Pattern.CASE_INSENSITIVE);

    private static final Pattern URL_EXTRACTION = Pattern.compile("((?:http|https|file|market)://|(?:inline|data|about|content|javascript|mailto|view-source|yuzu):)(\\S*)", Pattern.CASE_INSENSITIVE);

    public static boolean isUrl(String query) {
        query = query.trim();
        boolean hasSpace = (query.indexOf(' ') != -1);
        Matcher matcher = URI_SCHEMA.matcher(query);
        if (matcher.matches())
            return true;
        return !hasSpace && Patterns.WEB_URL.matcher(query).matches();
    }

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
                return false;
            default:
                return true;
        }
    }

    public static String makeUrlFromQuery(String query, String search_url, String search_place_holder) {
        query = query.trim();
        boolean hasSpace = (query.indexOf(' ') != -1);

        Matcher matcher = URI_SCHEMA.matcher(query);
        if (matcher.matches()) {
            String scheme = matcher.group(1);
            String lcScheme = scheme.toLowerCase(Locale.US);
            if (!lcScheme.equals(scheme)) {
                query = lcScheme + matcher.group(2);
            }
            return query;
        }
        if (!hasSpace && Patterns.WEB_URL.matcher(query).matches()) {
            return URLUtil.guessUrl(query);
        }
        return URLUtil.composeSearchUrl(query, search_url, search_place_holder);
    }

    public static String makeSearchUrlFromQuery(String query, String search_url, String search_place_holder) {
        return URLUtil.composeSearchUrl(query.trim(), search_url, search_place_holder);
    }

    public static String makeUrl(String query) {
        query = query.trim();
        boolean hasSpace = (query.indexOf(' ') != -1);

        Matcher matcher = URI_SCHEMA.matcher(query);
        if (matcher.matches()) {
            String scheme = matcher.group(1);
            String lcScheme = scheme.toLowerCase(Locale.US);
            if (!lcScheme.equals(scheme)) {
                query = lcScheme + matcher.group(2);
            }
            if (hasSpace && Patterns.WEB_URL.matcher(query).matches()) {
                query = query.replace(" ", "%20");
            }
            return query;
        }
        return URLUtil.guessUrl(query);
    }

    public static Intent createShareWebIntent(String url, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        if (title != null)
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
        return intent;
    }

    public static Intent createShareWebIntent(Intent intent, String url) {
        intent.putExtra(Intent.EXTRA_TEXT, url);
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

    public static Pattern makeUrlPattern(String pattern_url) {
        try {
            return makeUrlPatternWithThrow(pattern_url);
        } catch (PatternSyntaxException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return null;
    }

    public static Pattern makeUrlPatternWithThrow(String pattern_url) {
        if (pattern_url == null) return null;
        if (!pattern_url.startsWith("?")) {
            pattern_url = pattern_url.replace("?", "\\?").replace(".", "\\.").replace("*", ".*?").replace("+", ".+?");

            if (maybeContainsUrlScheme(pattern_url))
                return Pattern.compile("^" + pattern_url);
            else
                return Pattern.compile("^\\w+://" + pattern_url);
        } else {
            pattern_url = pattern_url.substring(1);
            return Pattern.compile(pattern_url);
        }
    }

    private static final Pattern sSchemeContainsPattern = Pattern.compile("^\\w+:", Pattern.CASE_INSENSITIVE);

    public static boolean maybeContainsUrlScheme(String url) {
        return url != null && sSchemeContainsPattern.matcher(url).find();
    }
}
