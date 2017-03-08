package jp.hazuki.yuzubrowser.pattern.url;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

import jp.hazuki.yuzubrowser.pattern.PatternManager;

public class PatternUrlManager extends PatternManager<PatternUrlChecker> {
    public PatternUrlManager(Context context) {
        super(context, "url_1.dat");
    }

    @Override
    protected PatternUrlChecker newInstance(JsonParser parser) throws IOException {
        return new PatternUrlChecker(parser);
    }
}
