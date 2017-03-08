package jp.hazuki.yuzubrowser.pattern.url;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.pattern.PatternAction;
import jp.hazuki.yuzubrowser.pattern.PatternChecker;
import jp.hazuki.yuzubrowser.utils.WebUtils;

public class PatternUrlChecker extends PatternChecker {
    private static final String FIELD_PATTERN_URL = "0";
    private String mPatternUrl;
    private Pattern mPattern;

    public PatternUrlChecker(PatternAction pattern_action, String pattern_url) throws PatternSyntaxException {
        super(pattern_action);
        setPatternUrlWithThrow(pattern_url);
    }

    public PatternUrlChecker(JsonParser parser) throws PatternSyntaxException, IOException {
        super(PatternAction.newInstance(parser));
        if (parser.nextToken() != JsonToken.START_OBJECT) return;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
            if (FIELD_PATTERN_URL.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                setPatternUrlWithThrow(parser.getText());
                continue;
            }
            parser.skipChildren();
        }
    }

    public final String getPatternUrl() {
        return mPatternUrl;
    }

    private void setPatternUrlWithThrow(String pattern_url) throws PatternSyntaxException {
        mPattern = WebUtils.makeUrlPatternWithThrow(pattern_url);
        this.mPatternUrl = pattern_url;
    }

    public boolean isMatchUrl(String url) {
        return mPattern.matcher(url).find();
    }

    protected Matcher matcher(String url) {
        return mPattern.matcher(url);
    }

    @Override
    public String getTitle(Context context) {
        return mPatternUrl;
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        getAction().write(generator);
        generator.writeStartObject();
        generator.writeStringField(FIELD_PATTERN_URL, mPatternUrl);
        generator.writeEndObject();
        return true;
    }
}
