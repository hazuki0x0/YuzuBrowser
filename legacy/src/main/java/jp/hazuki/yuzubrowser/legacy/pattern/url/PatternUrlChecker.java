package jp.hazuki.yuzubrowser.legacy.pattern.url;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction;
import jp.hazuki.yuzubrowser.legacy.pattern.PatternChecker;
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils;
import jp.hazuki.yuzubrowser.legacy.utils.fastmatch.FastMatcherFactory;

public class PatternUrlChecker extends PatternChecker {
    private static final String FIELD_PATTERN_URL = "0";
    private static final String FIELD_PATTERN_ENABLE = "1";
    private String mPatternUrl;
    private Pattern mPattern;
    private boolean enable = true;

    public PatternUrlChecker(PatternAction pattern_action, FastMatcherFactory factory, String pattern_url) throws PatternSyntaxException {
        super(pattern_action);
        setPatternUrlWithThrow(factory, pattern_url);
    }

    public PatternUrlChecker(JsonParser parser, FastMatcherFactory factory) throws PatternSyntaxException, IOException {
        super(PatternAction.newInstance(parser));
        //TODO not set mPattern
        if (parser.nextToken() != JsonToken.START_OBJECT) return;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
            if (FIELD_PATTERN_URL.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                setPatternUrlWithThrow(factory, parser.getText());
                continue;
            } else if (FIELD_PATTERN_ENABLE.equals(parser.getCurrentName())) {
                enable = parser.nextBooleanValue();
                continue;
            }
            parser.skipChildren();
        }
    }

    public final String getPatternUrl() {
        return mPatternUrl;
    }

    private void setPatternUrlWithThrow(FastMatcherFactory factory, String pattern_url) throws PatternSyntaxException {
        mPattern = WebUtils.makeUrlPatternWithThrow(factory, pattern_url);
        this.mPatternUrl = pattern_url;
    }

    public boolean isMatchUrl(String url) {
        return enable && mPattern != null && mPattern.matcher(url).find();
    }

    protected Matcher matcher(String url) {
        return mPattern.matcher(url);
    }

    @Override
    public String getTitle(Context context) {
        return mPatternUrl;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        getAction().write(generator);
        generator.writeStartObject();
        generator.writeStringField(FIELD_PATTERN_URL, mPatternUrl);
        generator.writeBooleanField(FIELD_PATTERN_ENABLE, enable);
        generator.writeEndObject();
        return true;
    }
}
