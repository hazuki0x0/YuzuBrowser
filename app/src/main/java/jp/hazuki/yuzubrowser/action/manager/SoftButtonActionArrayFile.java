package jp.hazuki.yuzubrowser.action.manager;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.action.ActionFile;
import jp.hazuki.yuzubrowser.action.SingleAction;

public class SoftButtonActionArrayFile extends ActionFile {
    private static final long serialVersionUID = -8451972340596132660L;

    private final String FOLDER_NAME;
    private final int id;
    public final List<SoftButtonActionFile> list = new ArrayList<>();

    public SoftButtonActionArrayFile(String folder_name, int id) {
        this.id = id;
        FOLDER_NAME = folder_name;
    }

    public void add(SingleAction action) {
        SoftButtonActionFile array = new SoftButtonActionFile();
        array.press.add(action);
        list.add(array);
    }

    public void add(SoftButtonActionFile action) {
        SoftButtonActionFile array = new SoftButtonActionFile();
        array.press.addAll(action.press);
        array.lpress.addAll(action.lpress);
        array.up.addAll(action.up);
        array.down.addAll(action.down);
        array.left.addAll(action.left);
        array.right.addAll(action.right);
        list.add(array);
    }

    public SoftButtonActionFile getActionList(int no) {
        if (no < 0)
            throw new IllegalArgumentException("no < 0");
        expand(no + 1);
        return list.get(no);
    }

    public int expand(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size < 0");
        for (int i = list.size() - size; i < 0; ++i)
            list.add(new SoftButtonActionFile());
        return list.size();
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
        if (parser.nextToken() != JsonToken.START_ARRAY) return false;
        for (; ; ) {
            SoftButtonActionFile action = new SoftButtonActionFile();
            if (!action.load(parser)) {
                if (parser.getCurrentToken() == JsonToken.END_ARRAY)
                    break;
                else
                    return false;
            }
            list.add(action);
        }
        return true;
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (SoftButtonActionFile action : list) {
            action.write(generator);
        }
        generator.writeEndArray();
        return true;
    }

}
