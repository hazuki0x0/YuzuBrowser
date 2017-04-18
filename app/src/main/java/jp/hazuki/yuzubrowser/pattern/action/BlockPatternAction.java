package jp.hazuki.yuzubrowser.pattern.action;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.PatternAction;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;

public class BlockPatternAction extends PatternAction {
    public BlockPatternAction() {
    }

    public BlockPatternAction(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
    }

    @Override
    public int getTypeId() {
        return BLOCK;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.pattern_block);
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeNumber(BLOCK);
        generator.writeNumber(0);
        return true;
    }

    @Override
    public boolean run(Context context, MainTabData tab, String url) {
        return true;
    }
}
