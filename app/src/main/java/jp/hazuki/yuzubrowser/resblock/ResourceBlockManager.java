package jp.hazuki.yuzubrowser.resblock;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParser;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.utils.matcher.AbstractPatternManager;

public class ResourceBlockManager extends AbstractPatternManager<ResourceChecker> {
    private static final String FOLDER_NAME = "resblock1";

    public ResourceBlockManager(Context context) {
        super(context, new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), "1.dat"));
    }

    @Override
    protected ResourceChecker newInstance(JsonParser parser) throws IOException {
        return ResourceChecker.newInstance(parser);
    }
}
