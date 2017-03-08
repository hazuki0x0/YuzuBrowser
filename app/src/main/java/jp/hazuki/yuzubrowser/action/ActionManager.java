package jp.hazuki.yuzubrowser.action;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.action.manager.FlickActionManager;
import jp.hazuki.yuzubrowser.action.manager.HardButtonActionManager;
import jp.hazuki.yuzubrowser.action.manager.LongPressActionManager;
import jp.hazuki.yuzubrowser.action.manager.MenuActionManager;
import jp.hazuki.yuzubrowser.action.manager.QuickControlActionManager;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionArrayManager;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionManager;
import jp.hazuki.yuzubrowser.action.manager.TabActionManager;
import jp.hazuki.yuzubrowser.action.manager.ToolbarActionManager;
import jp.hazuki.yuzubrowser.action.manager.WebSwipeActionManager;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;

public class ActionManager {
    public static final String INTENT_EXTRA_ACTION_TYPE = "ActionManager.extra.actionType";
    public static final String INTENT_EXTRA_ACTION_ID = "ActionManager.extra.actionId";

    //same as attrs.xml
    public static final int TYPE_SOFT_BUTTON = 1;
    public static final int TYPE_MENU = 2;
    public static final int TYPE_HARD_BUTTON = 3;
    public static final int TYPE_TAB = 4;
    public static final int TYPE_SOFT_BUTTON_CUSTOMBAR = 5;
    public static final int TYPE_LONGPRESS = 6;
    public static final int TYPE_FLICK = 7;
    public static final int TYPE_QUICK_CONTROL = 8;
    public static final int TYPE_WEB_SWIPE = 9;
    public static final int TYPE_SOFT_BUTTON_ARRAY = 10;

    public static ActionManager getActionManager(Context context, int type) {
        switch (type) {
            case TYPE_SOFT_BUTTON:
                return SoftButtonActionManager.getInstance(context);
            case TYPE_MENU:
                return MenuActionManager.getInstance(context);
            case TYPE_HARD_BUTTON:
                return HardButtonActionManager.getInstance(context);
            case TYPE_TAB:
                return TabActionManager.getInstance(context);
            case TYPE_SOFT_BUTTON_CUSTOMBAR:
                return ToolbarActionManager.getInstance(context);
            case TYPE_LONGPRESS:
                return LongPressActionManager.getInstance(context);
            case TYPE_FLICK:
                return FlickActionManager.getInstance(context);
            case TYPE_QUICK_CONTROL:
                return QuickControlActionManager.getInstance(context);
            case TYPE_WEB_SWIPE:
                return WebSwipeActionManager.getInstance(context);
            case TYPE_SOFT_BUTTON_ARRAY:
                return SoftButtonActionArrayManager.getInstance(context);
        }
        throw new IllegalArgumentException();
    }

    private static final String TAG = "ActionManagerBase";

    private ArrayList<ActionFile> getActionFileList() {
        ArrayList<ActionFile> list = new ArrayList<>();
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object obj = field.get(this);
                if (obj instanceof ActionFile) {
                    list.add((ActionFile) obj);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return list;
    }

    public boolean load(Context context) {
        for (ActionFile actions : getActionFileList()) {
            if (!actions.load(context)) {
                Logger.e(TAG, "load failed");
                return false;
            }
        }
        return true;
    }

    public boolean save(Context context) {
        for (ActionFile actions : getActionFileList()) {
            if (!actions.write(context)) {
                Logger.e(TAG, "save failed");
                return false;
            }
        }
        return true;
    }
}
