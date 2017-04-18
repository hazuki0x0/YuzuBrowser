package jp.hazuki.yuzubrowser.pattern;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.pattern.action.BlockPatternAction;
import jp.hazuki.yuzubrowser.pattern.action.OpenOthersPatternAction;
import jp.hazuki.yuzubrowser.pattern.action.WebSettingPatternAction;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.utils.matcher.AbstractPatternAction;

public abstract class PatternAction extends AbstractPatternAction {
    public static final int OPEN_OTHERS = 1;
    //public static final int REPLACE_URL = 2;
    public static final int WEB_SETTING = 3;
    public static final int BLOCK = 4;

    public abstract int getTypeId();

    public static PatternAction newInstance(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return null;
        int id = parser.getIntValue();
        switch (id) {
            case OPEN_OTHERS:
                return new OpenOthersPatternAction(parser);
            case WEB_SETTING:
                return new WebSettingPatternAction(parser);
            case BLOCK:
                return new BlockPatternAction(parser);
            default:
                throw new RuntimeException("unknown id : " + id);
        }
    }

    public abstract boolean run(Context context, MainTabData tab, String url);
}
