package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;

public class SoftButtonActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_btn";

    //the same in attrs.xml
    public static final int FIELD_BUTTON_TYPE = 0xFFF0;
    public static final int BUTTON_CENTER = 0x0040;

    public final SoftButtonActionFile btn_url_center = new SoftButtonActionFile(FOLDER_NAME, BUTTON_CENTER);

    @Override
    public Action getAction(int id) {
        switch (id & FIELD_BUTTON_TYPE) {
            case BUTTON_CENTER:
                return btn_url_center.getAction(id);
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static SoftButtonActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SoftButtonActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static SoftButtonActionManager sInstance = null;
}
