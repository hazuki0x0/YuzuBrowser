package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SoftButtonActionArrayManagerBase;

public class ToolbarActionManager extends SoftButtonActionArrayManagerBase {
    private static final String FOLDER_NAME = "action1_custombar";

    //the same in attrs.xml
    public static final int FIELD_ID = 0xFF000000;
    public static final int FIELD_NO = 0x00FF0000;
    public static final int FIELD_ID_SHIFT = 6 * 4;
    public static final int FIELD_NO_SHIFT = 4 * 4;
    public static final int CUSTOMBAR_1 = 0x01000000;


    public final SoftButtonActionArrayFile custombar1 = new SoftButtonActionArrayFile(FOLDER_NAME, CUSTOMBAR_1);

    @Override
    public Action getAction(int id) {
        return getActionArrayFile(id).getActionList((id & FIELD_NO) >> FIELD_NO_SHIFT).getAction(id);
    }

    @Override
    public SoftButtonActionArrayFile getActionArrayFile(int id) {
        switch (id & FIELD_ID) {
            case CUSTOMBAR_1:
                return custombar1;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    @Override
    public int makeActionIdFromPosition(int id, int position) {
        return id | (position << FIELD_NO_SHIFT);
    }

    public static ToolbarActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ToolbarActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static ToolbarActionManager sInstance = null;
}
