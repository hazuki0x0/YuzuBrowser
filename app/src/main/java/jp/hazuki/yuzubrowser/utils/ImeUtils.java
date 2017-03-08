package jp.hazuki.yuzubrowser.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class ImeUtils {
    private ImeUtils() {
        throw new UnsupportedOperationException();
    }

    public static void hideIme(Context context, View edittext) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(edittext.getWindowToken(), 0);
    }

    public static void showIme(Context context, View edittext) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(edittext, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isImeShown(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        if (rect.top != 0 || rect.bottom != 0) {
            if (rect.bottom - rect.top < DisplayUtils.getDisplayHeight(activity) * 0.7)
                return true;
        }
        return false;
    }
}
