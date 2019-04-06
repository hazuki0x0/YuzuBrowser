package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;

public class DoubleTapFlickActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_double_flick";

    public static final int FLICK_LEFT = 0x01;
    public static final int FLICK_RIGHT = 0x02;
    public static final int FLICK_UP = 0x03;
    public static final int FLICK_DOWN = 0x04;

    //the same in attrs.xml
    public final SingleActionFile flick_left = new SingleActionFile(FOLDER_NAME, FLICK_LEFT);
    public final SingleActionFile flick_right = new SingleActionFile(FOLDER_NAME, FLICK_RIGHT);
    public final SingleActionFile flick_up = new SingleActionFile(FOLDER_NAME, FLICK_UP);
    public final SingleActionFile flick_down = new SingleActionFile(FOLDER_NAME, FLICK_DOWN);

    @Override
    public Action getAction(int id) {
        switch (id) {
            case FLICK_LEFT:
                return flick_left.action;
            case FLICK_RIGHT:
                return flick_right.action;
            case FLICK_UP:
                return flick_up.action;
            case FLICK_DOWN:
                return flick_down.action;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static DoubleTapFlickActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DoubleTapFlickActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static DoubleTapFlickActionManager sInstance = null;
}
