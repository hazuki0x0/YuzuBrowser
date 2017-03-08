package jp.hazuki.yuzubrowser.action.manager;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.action.ActionFile;
import jp.hazuki.yuzubrowser.action.ActionList;

public class ActionArrayFile extends ActionFile {
    private static final long serialVersionUID = 6536274056164364431L;

    private final String FOLDER_NAME;

    private final int id;
    public final ActionList list = new ActionList();

    public ActionArrayFile(String folder_name, int id) {
        this.id = id;
        FOLDER_NAME = folder_name;
    }

    public ActionList getList() {
        return list;
    }

    @Override
    public File getFile(Context context) {
        return new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id + ".dat");
    }

    @Override
    public void reset() {
        list.clear();
    }

    @Override
    public boolean load(JsonParser parser) throws IOException {
        return list.loadAction(parser);
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        list.writeAction(generator);
        return true;
    }
}
