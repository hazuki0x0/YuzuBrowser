package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;
import android.content.SharedPreferences;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionList;
import jp.hazuki.yuzubrowser.legacy.action.SingleAction;
import jp.hazuki.yuzubrowser.legacy.action.SingleActionManager;
import jp.hazuki.yuzubrowser.legacy.action.item.CustomMenuSingleAction;

public class LongPressActionManager extends SingleActionManager {
    private static final String FOLDER_NAME = "action1_lpress";

    //the same in attrs.xml
    public static final int TYPE_OTHERS = 0x0001;
    public static final int TYPE_LINK = 0x0002;
    public static final int TYPE_IMAGE = 0x0003;
    public static final int TYPE_IMAGE_LINK = 0x0004;

    public final SingleActionFile others = new SingleActionFile(FOLDER_NAME, TYPE_OTHERS);
    public final SingleActionFile link = new SingleActionFile(FOLDER_NAME, TYPE_LINK);
    public final SingleActionFile image = new SingleActionFile(FOLDER_NAME, TYPE_IMAGE);
    public final SingleActionFile image_link = new SingleActionFile(FOLDER_NAME, TYPE_IMAGE_LINK);

    @Override
    public Action getAction(int id) {
        switch (id) {
            case TYPE_OTHERS:
                return others.action;
            case TYPE_LINK:
                return link.action;
            case TYPE_IMAGE:
                return image.action;
            case TYPE_IMAGE_LINK:
                return image_link.action;
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static void compat(Context context, SharedPreferences shared_preference) {
        LongPressActionManager manager = LongPressActionManager.getInstance(context);

        switch (shared_preference.getInt("lpress_link", 1)) {
            case 1: {
                CustomMenuSingleAction action = (CustomMenuSingleAction) SingleAction.makeInstance(SingleAction.CUSTOM_MENU);
                ActionList list = action.getActionList();
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_LINK_TEXT));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_PAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_PATTERN_MATCH));
                manager.link.action.add(action);
            }
            break;
            case 100:
                manager.link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_NEW));
                break;
            case 101:
                manager.link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_BG));
                break;
        }

        switch (shared_preference.getInt("lpress_image", 1)) {
            case 1: {
                CustomMenuSingleAction action = (CustomMenuSingleAction) SingleAction.makeInstance(SingleAction.CUSTOM_MENU);
                ActionList list = action.getActionList();
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_IMAGE_RES_BLOCK));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_PATTERN_MATCH));
                manager.image.action.add(action);
            }
            break;
            case 200:
                manager.image.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE));
                break;
            case 201:
                manager.image.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_NEW));
                break;
            case 202:
                manager.image.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_BG));
                break;
            case 210:
                manager.image.action.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE_AS));
                break;
        }

        switch (shared_preference.getInt("lpress_linkimage", 1)) {
            case 1: {
                CustomMenuSingleAction action = (CustomMenuSingleAction) SingleAction.makeInstance(SingleAction.CUSTOM_MENU);
                ActionList list = action.getActionList();
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_PAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_NEW));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_BG));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SHARE_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_OTHERS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_COPY_IMAGE_URL));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE_AS));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_IMAGE_RES_BLOCK));
                list.add(SingleAction.makeInstance(SingleAction.LPRESS_PATTERN_MATCH));
                manager.image_link.action.add(action);
            }
            break;
            case 100:
                manager.image_link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_NEW));
                break;
            case 101:
                manager.image_link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_BG));
                break;
            case 200:
                manager.image_link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE));
                break;
            case 201:
                manager.image_link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_NEW));
                break;
            case 202:
                manager.image_link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_OPEN_IMAGE_BG));
                break;
            case 210:
                manager.image_link.action.add(SingleAction.makeInstance(SingleAction.LPRESS_SAVE_IMAGE_AS));
                break;
        }

        manager.save(context);
    }

    public static LongPressActionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LongPressActionManager();
            sInstance.load(context);
        }
        return sInstance;
    }

    private static LongPressActionManager sInstance = null;
}
