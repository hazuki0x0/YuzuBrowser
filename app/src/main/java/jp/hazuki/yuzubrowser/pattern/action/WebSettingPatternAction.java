package jp.hazuki.yuzubrowser.pattern.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebSettings;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.PatternAction;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;

public class WebSettingPatternAction extends PatternAction {
    public static final int UNDEFINED = 0;
    public static final int ENABLE = 1;
    public static final int DISABLE = 2;

    private static final String FIELD_NAME_UA = "0";
    private static final String FIELD_NAME_JS = "1";

    private String mUserAgent;
    private int mJavaScript;

    public WebSettingPatternAction(String ua, int js) {
        mUserAgent = ua;
        mJavaScript = js;
    }

    public WebSettingPatternAction(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) return;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
            if (FIELD_NAME_UA.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                mUserAgent = parser.getText();
                continue;
            }
            if (FIELD_NAME_JS.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                mJavaScript = parser.getIntValue();
                continue;
            }
            parser.skipChildren();
        }
    }

    @Override
    public int getTypeId() {
        return WEB_SETTING;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.pattern_change_websettings);
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeNumber(WEB_SETTING);
        generator.writeStartObject();
        if (mUserAgent != null)
            generator.writeStringField(FIELD_NAME_UA, mUserAgent);
        generator.writeNumberField(FIELD_NAME_JS, mJavaScript);
        generator.writeEndObject();
        return true;
    }

    public String getUserAgentString() {
        return mUserAgent;
    }

    public int getJavaScriptSetting() {
        return mJavaScript;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public boolean run(Context context, MainTabData tab, String url) {
        WebSettings settings = tab.mWebView.getSettings();

        if (mUserAgent != null)
            settings.setUserAgentString(mUserAgent);

        switch (mJavaScript) {
            case ENABLE:
                settings.setJavaScriptEnabled(true);
                break;
            case DISABLE:
                settings.setJavaScriptEnabled(false);
                break;
        }
        return false;
    }
}
