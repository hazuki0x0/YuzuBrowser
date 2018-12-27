package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;

public class HardButtonActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_hbtn";

    //the same in attrs.xml
    private static final int BUTTON_BACK_PRESS = 0x01000000;
    private static final int BUTTON_BACK_LPRESS = 0x02000000;
    private static final int BUTTON_SEARCH_PRESS = 0x03000000;
    private static final int BUTTON_VOLUME_UP = 0x04000000;
    private static final int BUTTON_VOLUME_DOWN = 0x05000000;
    private static final int BUTTON_CAMERA_PRESS = 0x06000000;

    public final SingleActionFile back_press = new SingleActionFile(FOLDER_NAME, BUTTON_BACK_PRESS);
    public final SingleActionFile back_lpress = new SingleActionFile(FOLDER_NAME, BUTTON_BACK_LPRESS);
    public final SingleActionFile search_press = new SingleActionFile(FOLDER_NAME, BUTTON_SEARCH_PRESS);
    public final SingleActionFile volume_up = new SingleActionFile(FOLDER_NAME, BUTTON_VOLUME_UP);
    public final SingleActionFile volume_down = new SingleActionFile(FOLDER_NAME, BUTTON_VOLUME_DOWN);
    public final SingleActionFile camera_press = new SingleActionFile(FOLDER_NAME, BUTTON_CAMERA_PRESS);

    @Override
    public Action getAction(int id) {
        switch (id) {
            case BUTTON_BACK_PRESS:
                return back_press.action;
            case BUTTON_BACK_LPRESS:
                return back_lpress.action;
            case BUTTON_SEARCH_PRESS:
                return search_press.action;
            case BUTTON_VOLUME_UP:
                return volume_up.action;
            case BUTTON_VOLUME_DOWN:
                return volume_down.action;
            case BUTTON_CAMERA_PRESS:
                return camera_press.action;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static HardButtonActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HardButtonActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static HardButtonActionManager sInstance = null;
}
