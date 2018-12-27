package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;

public class WebSwipeActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_webswipe";

    public static final int SWIPE_UP = 0x0003;
    public static final int SWIPE_DOWN = 0x0004;
    public static final int SWIPE_LEFT = 0x0005;
    public static final int SWIPE_RIGHT = 0x0006;

    //the same in attrs.xml
    public final SingleActionFile double_up = new SingleActionFile(FOLDER_NAME, SWIPE_UP);
    public final SingleActionFile double_down = new SingleActionFile(FOLDER_NAME, SWIPE_DOWN);
    public final SingleActionFile double_left = new SingleActionFile(FOLDER_NAME, SWIPE_LEFT);
    public final SingleActionFile double_right = new SingleActionFile(FOLDER_NAME, SWIPE_RIGHT);

    @Override
    public Action getAction(int id) {
        switch (id) {
            case SWIPE_UP:
                return double_up.action;
            case SWIPE_DOWN:
                return double_down.action;
            case SWIPE_LEFT:
                return double_left.action;
            case SWIPE_RIGHT:
                return double_right.action;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static WebSwipeActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WebSwipeActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static WebSwipeActionManager sInstance = null;
}
