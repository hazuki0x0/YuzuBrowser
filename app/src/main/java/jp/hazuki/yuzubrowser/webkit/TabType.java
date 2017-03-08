package jp.hazuki.yuzubrowser.webkit;

import android.support.annotation.IntDef;

/**
 * Created by hazuki on 17/02/07.
 */

@IntDef({TabType.DEFAULT, TabType.WINDOW, TabType.INTENT})
public @interface TabType {
    int DEFAULT = 0;
    int INTENT = 1;
    int WINDOW = 2;
}
