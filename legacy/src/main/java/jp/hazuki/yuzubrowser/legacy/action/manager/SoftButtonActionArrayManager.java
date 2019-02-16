package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SoftButtonActionArrayManagerBase;

public class SoftButtonActionArrayManager extends SoftButtonActionArrayManagerBase {
    private static final String FOLDER_NAME = "action1_sbtn_ary";

    //the same in attrs.xml
    public static final int FIELD_ID = 0xFFFF0000;
    public static final int FIELD_NO = 0x0000FFF0;
    public static final int FIELD_ID_SHIFT = 5 * 4;
    public static final int FIELD_NO_SHIFT = 2 * 4;
    public static final int BUTTON_TAB_LEFT = 0x00010000;
    public static final int BUTTON_TAB_RIGHT = 0x00020000;
    public static final int BUTTON_URL_LEFT = 0x00030000;
    public static final int BUTTON_URL_RIGHT = 0x00040000;

    public final SoftButtonActionArrayFile btn_tab_left = new SoftButtonActionArrayFile(FOLDER_NAME, BUTTON_TAB_LEFT);
    public final SoftButtonActionArrayFile btn_tab_right = new SoftButtonActionArrayFile(FOLDER_NAME, BUTTON_TAB_RIGHT);
    public final SoftButtonActionArrayFile btn_url_left = new SoftButtonActionArrayFile(FOLDER_NAME, BUTTON_URL_LEFT);
    public final SoftButtonActionArrayFile btn_url_right = new SoftButtonActionArrayFile(FOLDER_NAME, BUTTON_URL_RIGHT);

    @Override
    public Action getAction(int id) {
        return getActionArrayFile(id).getActionList((id & FIELD_NO) >> FIELD_NO_SHIFT).getAction(id);
    }

    @Override
    public SoftButtonActionArrayFile getActionArrayFile(int id) {
        switch (id & FIELD_ID) {
            case BUTTON_TAB_LEFT:
                return btn_tab_left;
            case BUTTON_TAB_RIGHT:
                return btn_tab_right;
            case BUTTON_URL_LEFT:
                return btn_url_left;
            case BUTTON_URL_RIGHT:
                return btn_url_right;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    @Override
    public int makeActionIdFromPosition(int id, int position) {
        return id | (position << FIELD_NO_SHIFT);
    }

    public static SoftButtonActionArrayManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SoftButtonActionArrayManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static SoftButtonActionArrayManager sInstance = null;
}
