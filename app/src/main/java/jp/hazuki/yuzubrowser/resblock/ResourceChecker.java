package jp.hazuki.yuzubrowser.resblock;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.utils.matcher.AbstractPatternChecker;

public abstract class ResourceChecker extends AbstractPatternChecker<ResourceData> {
    protected static final int NORMAL_CHECKER = 0;

    public static final int SHOULD_RUN = 0;
    public static final int SHOULD_BREAK = 1;
    public static final int SHOULD_CONTINUE = 2;

    protected ResourceChecker(ResourceData data) {
        super(data);
    }

    protected ResourceChecker(JsonParser parser) throws IOException {
        super(ResourceData.newInstance(parser));
    }

    public abstract int check(Uri url);

    public WebResourceResponse getResource(Context context) {
        return getAction().getResource(context);
    }

    public static ResourceChecker newInstance(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return null;
        switch (parser.getIntValue()) {
            case NORMAL_CHECKER:
                return new NormalChecker(parser);
        }
        return null;
    }
}
