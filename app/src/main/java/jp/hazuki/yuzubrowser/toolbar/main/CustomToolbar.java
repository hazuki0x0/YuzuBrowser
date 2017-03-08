package jp.hazuki.yuzubrowser.toolbar.main;

import android.content.Context;

import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager.RequestCallback;

public class CustomToolbar extends CustomToolbarBase {
    public CustomToolbar(Context context, ActionCallback action_callback, RequestCallback request_callback) {
        super(context, AppData.toolbar_custom1, action_callback, request_callback);
    }
}
