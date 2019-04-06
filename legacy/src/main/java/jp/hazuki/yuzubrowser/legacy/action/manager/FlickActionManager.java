package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;

public class FlickActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_flick";

    public static final int FLICK_LEFT = 0x01;
    public static final int FLICK_RIGHT = 0x02;

    //the same in attrs.xml
    public final SingleActionFile flick_left = new SingleActionFile(FOLDER_NAME, FLICK_LEFT);
    public final SingleActionFile flick_right = new SingleActionFile(FOLDER_NAME, FLICK_RIGHT);

    @Override
    public Action getAction(int id) {
        switch (id) {
            case FLICK_LEFT:
                return flick_left.action;
            case FLICK_RIGHT:
                return flick_right.action;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static FlickActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FlickActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static FlickActionManager sInstance = null;
}
