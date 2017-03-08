package jp.hazuki.yuzubrowser.action.manager;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionFile;

public class SingleActionFile extends ActionFile {
    private static final long serialVersionUID = 3216383296384940721L;

    private final String FOLDER_NAME;

    private final int id;
    public Action action = new Action();

    public SingleActionFile(String folder_name, int id) {
        this.id = id;
        FOLDER_NAME = folder_name;
    }

    @Override
    public File getFile(Context context) {
        return new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id + ".dat");
    }

    @Override
    public void reset() {
        action.clear();
    }

    @Override
    public boolean load(JsonParser parser) throws IOException {
        return action.loadAction(parser);
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        action.writeAction(generator);
        return true;
    }

}
