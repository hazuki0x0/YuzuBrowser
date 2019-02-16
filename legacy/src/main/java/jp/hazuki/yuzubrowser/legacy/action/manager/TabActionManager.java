package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;

public class TabActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_tab";

    //the same in attrs.xml
    public static final int TAB_UP = 0x0001;
    public static final int TAB_DOWN = 0x0002;
    public static final int TAB_PRESS = 0x0003;
    public static final int TAB_LPRESS = 0x0004;

    public final SingleActionFile tab_up = new SingleActionFile(FOLDER_NAME, TAB_UP);
    public final SingleActionFile tab_down = new SingleActionFile(FOLDER_NAME, TAB_DOWN);
    public final SingleActionFile tab_press = new SingleActionFile(FOLDER_NAME, TAB_PRESS);
    public final SingleActionFile tab_lpress = new SingleActionFile(FOLDER_NAME, TAB_LPRESS);

    @Override
    public Action getAction(int id) {
        switch (id) {
            case TAB_UP:
                return tab_up.action;
            case TAB_DOWN:
                return tab_down.action;
            case TAB_PRESS:
                return tab_press.action;
            case TAB_LPRESS:
                return tab_lpress.action;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static TabActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TabActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static TabActionManager sInstance = null;
}
