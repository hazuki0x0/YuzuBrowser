package jp.hazuki.yuzubrowser.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SearchUtils {
    private static final String GOOGLE_IMAGE_SEARCH = "https://www.google.com/searchbyimage?image_url=";

    public static String makeGoogleImageSearch(String imageUrl) {
        try {
            return GOOGLE_IMAGE_SEARCH + URLEncoder.encode(imageUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
