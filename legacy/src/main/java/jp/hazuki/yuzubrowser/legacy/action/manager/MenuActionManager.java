package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionList;
import jp.hazuki.yuzubrowser.legacy.action.ListActionManager;

public class MenuActionManager extends ListActionManager {
    private static final String FOLDER_NAME = "action1_menu";

    //the same in attrs.xml
    //private static final int FIELD_ID				= 0xFF000000;
    //private static final int FIELD_NO				= 0x00FFFFFF;
    private static final int MENU_BROWSER_ACTIVITY = 0x01000000;

    public final ActionArrayFile browser_activity = new ActionArrayFile(FOLDER_NAME, MENU_BROWSER_ACTIVITY);

    @Override
    public void addAction(int id, Action action) {
        getActionList(id).add(action);
    }

    @Override
    public ActionList getActionList(int id) {
        switch (id) {
            case MENU_BROWSER_ACTIVITY:
                return browser_activity.getList();
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static MenuActionManager getInstance(Context context) {
        MenuActionManager manager = new MenuActionManager();
        manager.load(context);
        return manager;
    }
}
