package jp.hazuki.yuzubrowser.action.manager;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionFile;

public class SoftButtonActionFile extends ActionFile {
    //private static final String TAG = "ButtonActionList";

    private static final long serialVersionUID = 2904009975751614292L;

    //the same in attrs.xml
    public static final int FIELD_SWIPE_TYPE = 0x000F;

    public static final int BUTTON_SWIPE_PRESS = 0x0001;
    public static final int BUTTON_SWIPE_LPRESS = 0x0002;
    public static final int BUTTON_SWIPE_UP = 0x0003;
    public static final int BUTTON_SWIPE_DOWN = 0x0004;
    public static final int BUTTON_SWIPE_LEFT = 0x0005;
    public static final int BUTTON_SWIPE_RIGHT = 0x0006;

    private final String FOLDER_NAME;

    private final int id;
    public final Action press = new Action();
    public final Action lpress = new Action();
    public final Action up = new Action();
    public final Action down = new Action();
    public final Action left = new Action();
    public final Action right = new Action();

    public SoftButtonActionFile() {
        this.id = 0;
        FOLDER_NAME = null;
    }

    public SoftButtonActionFile(String folder_name, int id) {
        this.id = id;
        FOLDER_NAME = folder_name;
    }

    public Action getAction(int search_id) {
        //if((search_id & FIELD_BUTTON_TYPE) != id) return null;
        switch (search_id & FIELD_SWIPE_TYPE) {
            case BUTTON_SWIPE_PRESS:
                return press;
            case BUTTON_SWIPE_LPRESS:
                return lpress;
            case BUTTON_SWIPE_UP:
                return up;
            case BUTTON_SWIPE_DOWN:
                return down;
            case BUTTON_SWIPE_LEFT:
                return left;
            case BUTTON_SWIPE_RIGHT:
                return right;
            default:
                throw new IllegalArgumentException("Unknown id:" + search_id);
        }
    }

    @Override
    public File getFile(Context context) {
        return new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id + ".dat");
    }

    @Override
    public void reset() {
        press.clear();
        lpress.clear();
        up.clear();
        down.clear();
        left.clear();
        right.clear();
    }

    @Override
    public boolean load(JsonParser parser) throws IOException {
        if (parser.nextValue() != JsonToken.START_ARRAY) return false;
        if (!press.loadAction(parser)) return false;
        if (!lpress.loadAction(parser)) return false;
        if (!up.loadAction(parser)) return false;
        if (!down.loadAction(parser)) return false;
        if (!left.loadAction(parser)) return false;
        if (!right.loadAction(parser)) return false;
        return parser.nextValue() == JsonToken.END_ARRAY;
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        press.writeAction(generator);
        lpress.writeAction(generator);
        up.writeAction(generator);
        down.writeAction(generator);
        left.writeAction(generator);
        right.writeAction(generator);
        generator.writeEndArray();
        return true;
    }
}
