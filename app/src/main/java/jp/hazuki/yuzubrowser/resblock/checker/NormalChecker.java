package jp.hazuki.yuzubrowser.resblock.checker;

import android.content.Context;
import android.net.Uri;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.resblock.ResourceChecker;
import jp.hazuki.yuzubrowser.resblock.ResourceData;

public class NormalChecker extends ResourceChecker {
    private static final String FIELD_URL = "0";
    private static final String FIELD_WHITE = "1";

    private String mUrl;
    private boolean mIsWhite;

    public NormalChecker(ResourceData data, String url, boolean isWhite) {
        super(data);
        mUrl = url;
        mIsWhite = isWhite;
    }

    public NormalChecker(JsonParser parser) throws IOException {
        super(parser);
        if (parser.nextToken() != JsonToken.START_OBJECT) return;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
            if (FIELD_URL.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                mUrl = parser.getText();
                continue;
            }
            if (FIELD_WHITE.equals(parser.getCurrentName())) {
                switch (parser.nextToken()) {
                    case VALUE_TRUE:
                        mIsWhite = true;
                        break;
                    case VALUE_FALSE:
                        mIsWhite = false;
                        break;
                }
                continue;
            }
            parser.skipChildren();
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isWhite() {
        return mIsWhite;
    }

    public String getTitle(Context context) {
        return mUrl;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void setEnable(boolean enable) {
    }

    @Override
    public int check(Uri url) {
        if (!url.toString().contains(mUrl))
            return SHOULD_CONTINUE;
        if (mIsWhite)
            return SHOULD_BREAK;
        else
            return SHOULD_RUN;
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeNumber(NORMAL_CHECKER);
        getAction().write(generator);
        generator.writeStartObject();
        generator.writeStringField(FIELD_URL, mUrl);
        generator.writeBooleanField(FIELD_WHITE, mIsWhite);
        generator.writeEndObject();
        return true;
    }
}
