package jp.hazuki.yuzubrowser.pattern.url;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

import jp.hazuki.yuzubrowser.pattern.PatternManager;
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherFactory;

public class PatternUrlManager extends PatternManager<PatternUrlChecker> {

    private FastMatcherFactory factory;

    public PatternUrlManager(Context context) {
        super(context, "url_1.dat");
        factory = new FastMatcherFactory();
    }

    @Override
    protected PatternUrlChecker newInstance(JsonParser parser) throws IOException {
        if (factory == null) factory = new FastMatcherFactory();
        return new PatternUrlChecker(parser, factory);
    }

    @Override
    public boolean load(Context context) {
        boolean result = super.load(context);
        if (factory != null)
            factory.release();
        return result;
    }
}
