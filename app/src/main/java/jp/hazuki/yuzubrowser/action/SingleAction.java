package jp.hazuki.yuzubrowser.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

import jp.hazuki.yuzubrowser.action.item.AutoPageScrollAction;
import jp.hazuki.yuzubrowser.action.item.CloseAutoSelectAction;
import jp.hazuki.yuzubrowser.action.item.CloseTabSingleAction;
import jp.hazuki.yuzubrowser.action.item.CustomMenuSingleAction;
import jp.hazuki.yuzubrowser.action.item.CustomSingleAction;
import jp.hazuki.yuzubrowser.action.item.FinishSingleAction;
import jp.hazuki.yuzubrowser.action.item.GoBackSingleAction;
import jp.hazuki.yuzubrowser.action.item.LeftRightTabSingleAction;
import jp.hazuki.yuzubrowser.action.item.MousePointerSingleAction;
import jp.hazuki.yuzubrowser.action.item.OpenOptionsMenuAction;
import jp.hazuki.yuzubrowser.action.item.OpenUrlSingleAction;
import jp.hazuki.yuzubrowser.action.item.PasteGoSingleAction;
import jp.hazuki.yuzubrowser.action.item.PasteSearchBoxAction;
import jp.hazuki.yuzubrowser.action.item.SaveScreenshotSingleAction;
import jp.hazuki.yuzubrowser.action.item.ShareScreenshotSingleAction;
import jp.hazuki.yuzubrowser.action.item.ShowSearchBoxAction;
import jp.hazuki.yuzubrowser.action.item.TabListSingleAction;
import jp.hazuki.yuzubrowser.action.item.ToastAction;
import jp.hazuki.yuzubrowser.action.item.TranslatePageSingleAction;
import jp.hazuki.yuzubrowser.action.item.VibrationSingleAction;
import jp.hazuki.yuzubrowser.action.item.WebScrollSingleAction;
import jp.hazuki.yuzubrowser.action.item.startactivity.StartActivitySingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class SingleAction implements Parcelable {
    public static final int GO_BACK = 1000;
    public static final int GO_FORWARD = 1001;
    public static final int WEB_RELOAD_STOP = 1005;
    public static final int WEB_RELOAD = 1006;
    public static final int WEB_STOP = 1007;
    public static final int GO_HOME = 1020;
    public static final int ZOOM_IN = 1205;
    public static final int ZOOM_OUT = 1206;
    public static final int PAGE_UP = 1207;
    public static final int PAGE_DOWN = 1208;
    public static final int PAGE_TOP = 1209;
    public static final int PAGE_BOTTOM = 1210;
    public static final int PAGE_SCROLL = 1215;
    public static final int PAGE_FAST_SCROLL = 1216;
    public static final int PAGE_AUTO_SCROLL = 1217;
    public static final int FOCUS_UP = 1220;
    public static final int FOCUS_DOWN = 1221;
    public static final int FOCUS_LEFT = 1222;
    public static final int FOCUS_RIGHT = 1223;
    public static final int FOCUS_CLICK = 1224;
    public static final int TOGGLE_JS = 2000;
    public static final int TOGGLE_IMAGE = 2001;
    public static final int TOGGLE_COOKIE = 2002;
    public static final int TOGGLE_USERJS = 2200;
    public static final int TOGGLE_NAV_LOCK = 2300;
    public static final int PAGE_INFO = 5000;
    public static final int COPY_URL = 5001;
    public static final int COPY_TITLE = 5002;
    public static final int COPY_TITLE_URL = 5003;
    public static final int TAB_HISTORY = 5010;
    public static final int MOUSE_POINTER = 5015;
    public static final int FIND_ON_PAGE = 5020;
    public static final int SAVE_SCREENSHOT = 5030;
    public static final int SHARE_SCREENSHOT = 5031;
    public static final int SAVE_PAGE = 5035;
    public static final int OPEN_URL = 5200;
    public static final int TRANSLATE_PAGE = 5300;
    public static final int NEW_TAB = 10000;
    public static final int CLOSE_TAB = 10001;
    public static final int CLOSE_ALL = 10002;
    public static final int CLOSE_OTHERS = 10003;
    public static final int CLOSE_AUTO_SELECT = 10100;
    public static final int LEFT_TAB = 10005;
    public static final int RIGHT_TAB = 10006;
    public static final int SWAP_LEFT_TAB = 10007;
    public static final int SWAP_RIGHT_TAB = 10008;
    public static final int TAB_LIST = 10010;
    public static final int CLOSE_ALL_LEFT = 10015;
    public static final int CLOSE_ALL_RIGHT = 10016;
    public static final int RESTORE_TAB = 10020;
    public static final int REPLICATE_TAB = 10021;
    public static final int SHOW_SEARCHBOX = 35000;
    public static final int PASTE_SEARCHBOX = 35001;
    public static final int PASTE_GO = 35002;
    public static final int SHOW_BOOKMARK = 35010;
    public static final int SHOW_HISTORY = 35011;
    public static final int SHOW_DOWNLOADS = 35012;
    public static final int SHOW_SETTINGS = 35013;
    public static final int OPEN_SPEED_DIAL = 35014;
    public static final int ADD_BOOKMARK = 35020;
    public static final int ADD_SPEED_DIAL = 35021;
    public static final int ADD_PATTERN = 35022;
    public static final int SUB_GESTURE = 35031;
    public static final int CLEAR_DATA = 35300;
    public static final int SHOW_PROXY_SETTING = 35301;
    public static final int ORIENTATION_SETTING = 35302;
    public static final int OPEN_LINK_SETTING = 35304;
    public static final int USERAGENT_SETTING = 35305;
    public static final int TEXTSIZE_SETTING = 35306;
    public static final int USERJS_SETTING = 35307;
    public static final int WEB_ENCODE_SETTING = 35308;
    public static final int DEFALUT_USERAGENT_SETTING = 35309;
    public static final int RENDER_SETTING = 35400;
    public static final int TOGGLE_VISIBLE_TAB = 38000;
    public static final int TOGGLE_VISIBLE_URL = 38001;
    public static final int TOGGLE_VISIBLE_PROGRESS = 38002;
    public static final int TOGGLE_VISIBLE_CUSTOM = 38003;
    public static final int TOGGLE_WEB_TITLEBAR = 38010;
    public static final int TOGGLE_WEB_GESTURE = 38100;
    public static final int TOGGLE_FLICK = 38101;
    public static final int TOGGLE_QUICK_CONTROL = 38102;
    public static final int TOGGLE_MULTI_FINGER_GESTURE = 38103;
    public static final int SHARE_WEB = 50000;
    public static final int OPEN_OTHER = 50001;
    public static final int START_ACTIVITY = 50005;
    public static final int TOGGLE_FULL_SCREEN = 50100;
    public static final int OPEN_OPTIONS_MENU = 50120;
    public static final int CUSTOM_MENU = 80000;
    public static final int FINISH = 90001;
    public static final int MINIMIZE = 90005;
    public static final int CUSTOM_ACTION = 100000;
    public static final int VIBRATION = 100100;
    public static final int TOAST = 100101;
    public static final int PRIVATE = 100110;
    public static final int VIEW_SOURCE = 101000;
    public static final int PRINT = 101010;
    public static final int TAB_PINNING = 101020;
    public static final int ALL_ACTION = 101030;
    public static final int LPRESS_OPEN = -10;
    public static final int LPRESS_OPEN_NEW = -11;
    public static final int LPRESS_OPEN_BG = -12;
    public static final int LPRESS_OPEN_NEW_RIGHT = -13;
    public static final int LPRESS_OPEN_BG_RIGHT = -14;
    public static final int LPRESS_SHARE = -50;
    public static final int LPRESS_OPEN_OTHERS = -51;
    public static final int LPRESS_COPY_URL = -52;
    public static final int LPRESS_SAVE_PAGE_AS = -53;
    public static final int LPRESS_SAVE_PAGE = -54;
    public static final int LPRESS_OPEN_IMAGE = -110;
    public static final int LPRESS_OPEN_IMAGE_NEW = -111;
    public static final int LPRESS_OPEN_IMAGE_BG = -112;
    public static final int LPRESS_OPEN_IMAGE_NEW_RIGHT = -113;
    public static final int LPRESS_OPEN_IMAGE_BG_RIGHT = -114;
    public static final int LPRESS_SHARE_IMAGE_URL = -150;
    public static final int LPRESS_OPEN_IMAGE_OTHERS = -151;
    public static final int LPRESS_COPY_IMAGE_URL = -152;
    public static final int LPRESS_SAVE_IMAGE_AS = -153;
    public static final int LPRESS_GOOGLE_IMAGE_SEARCH = -154;
    public static final int LPRESS_IMAGE_RES_BLOCK = -155;
    public static final int LPRESS_PATTERN_MATCH = -156;
    public static final int LPRESS_COPY_LINK_TEXT = -157;
    public static final int LPRESS_SHARE_IMAGE = -158;
    public static final int LPRESS_SAVE_IMAGE = -159;
    public static final int LPRESS_ADD_BLACK_LIST = -160;
    public static final int LPRESS_ADD_IMAGE_BLACK_LIST = -161;
    public static final int LPRESS_ADD_WHITE_LIST = -162;
    public static final int LPRESS_ADD_IMAGE_WHITE_LIST = -163;


    public final int id;

    public static SingleAction makeInstance(int id) {
        try {
            return makeInstance(id, null);
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        throw new IllegalStateException();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
    }

    protected SingleAction(Parcel source) {
        this.id = source.readInt();
    }

    public static final Creator<SingleAction> CREATOR = new Creator<SingleAction>() {
        @Override
        public SingleAction createFromParcel(Parcel source) {
            return new SingleAction(source);
        }

        @Override
        public SingleAction[] newArray(int size) {
            return new SingleAction[size];
        }
    };

    public static SingleAction makeInstance(int id, JsonParser parser) throws IOException {
        switch (id) {
            case GO_BACK:
                return new GoBackSingleAction(id, parser);
            case PAGE_SCROLL:
                return new WebScrollSingleAction(id, parser);
            case PAGE_AUTO_SCROLL:
                return new AutoPageScrollAction(id, parser);
            case MOUSE_POINTER:
                return new MousePointerSingleAction(id, parser);
            case SAVE_SCREENSHOT:
                return new SaveScreenshotSingleAction(id, parser);
            case SHARE_SCREENSHOT:
                return new ShareScreenshotSingleAction(id, parser);
            case OPEN_URL:
                return new OpenUrlSingleAction(id, parser);
            case TRANSLATE_PAGE:
                return new TranslatePageSingleAction(id, parser);
            case CLOSE_TAB:
                return new CloseTabSingleAction(id, parser);
            case LEFT_TAB:
            case RIGHT_TAB:
                return new LeftRightTabSingleAction(id, parser);
            case TAB_LIST:
                return new TabListSingleAction(id, parser);
            case SHOW_SEARCHBOX:
                return new ShowSearchBoxAction(id, parser);
            case PASTE_SEARCHBOX:
                return new PasteSearchBoxAction(id, parser);
            case PASTE_GO:
                return new PasteGoSingleAction(id, parser);
            case START_ACTIVITY:
                return new StartActivitySingleAction(id, parser);
            case OPEN_OPTIONS_MENU:
                return new OpenOptionsMenuAction(id, parser);
            case CUSTOM_MENU:
                return new CustomMenuSingleAction(id, parser);
            case FINISH:
                return new FinishSingleAction(id, parser);
            case CUSTOM_ACTION:
                return new CustomSingleAction(id, parser);
            case VIBRATION:
                return new VibrationSingleAction(id, parser);
            case TOAST:
                return new ToastAction(id, parser);
            case CLOSE_AUTO_SELECT:
                return new CloseAutoSelectAction(id, parser);
            default:
                return new SingleAction(id, parser);
        }
    }

    public static boolean checkSubPreference(int id) {
        switch (id) {
            case GO_BACK:
            case PAGE_SCROLL:
            case PAGE_AUTO_SCROLL:
            case MOUSE_POINTER:
            case SAVE_SCREENSHOT:
            case SHARE_SCREENSHOT:
            case OPEN_URL:
            case TRANSLATE_PAGE:
            case CLOSE_TAB:
            case LEFT_TAB:
            case RIGHT_TAB:
            case TAB_LIST:
            case SHOW_SEARCHBOX:
            case PASTE_SEARCHBOX:
            case PASTE_GO:
            case START_ACTIVITY:
            case OPEN_OPTIONS_MENU:
            case CUSTOM_MENU:
            case FINISH:
            case CUSTOM_ACTION:
            case VIBRATION:
            case TOAST:
            case CLOSE_AUTO_SELECT:
                return true;
            default:
                return false;
        }
    }

    //for extended class
    protected SingleAction(int id) {
        this.id = id;
        //if(id < 0) throw new IllegalArgumentException();
    }

    //node can be null
    private SingleAction(int id, JsonParser parser) throws IOException {
        this(id);
        if (parser != null) parser.nextToken();
    }

    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeNull();
    }

    public StartActivityInfo showMainPreference(ActionActivity context) {
        return null;
    }

    public StartActivityInfo showSubPreference(ActionActivity context) {
        return null;
    }

    public String toString(ActionNameArray nameArray) {
        int i = 0;
        for (int value : nameArray.actionValues) {
            if (value == id)
                return nameArray.actionList[i];
            ++i;
        }
        return null;
    }
}
