package jp.hazuki.yuzubrowser.webkit;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;

public abstract class CustomOnCreateContextMenuListener implements OnCreateContextMenuListener {
    public abstract void onCreateContextMenu(ContextMenu menu, CustomWebView v, ContextMenuInfo menuInfo);

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v instanceof CustomWebView)
            onCreateContextMenu(menu, (CustomWebView) v, menuInfo);
        else
            throw new IllegalArgumentException();
    }
}
