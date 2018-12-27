package jp.hazuki.yuzubrowser.legacy.resblock;

import android.content.Context;
import android.webkit.WebResourceResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyImageData;
import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyStringData;
import jp.hazuki.yuzubrowser.legacy.utils.matcher.AbstractPatternAction;

public abstract class ResourceData extends AbstractPatternAction {
    protected static final int EMPTY_STRING_DATA = 0;
    protected static final int EMPTY_IMAGE_DATA = 1;

    public abstract int getTypeId();

    public abstract WebResourceResponse getResource(Context context);

    public static ResourceData newInstance(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return null;
        switch (parser.getIntValue()) {
            case EMPTY_STRING_DATA:
                return new EmptyStringData(parser);
            case EMPTY_IMAGE_DATA:
                return new EmptyImageData(parser);
        }
        return null;
    }
}
