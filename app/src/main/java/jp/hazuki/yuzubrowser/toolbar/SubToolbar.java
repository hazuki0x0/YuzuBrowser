package jp.hazuki.yuzubrowser.toolbar;

import android.content.Context;

import jp.hazuki.yuzubrowser.theme.ThemeData;

public class SubToolbar extends AbstractToolbar {
    public SubToolbar(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyTheme(ThemeData.getInstance());
    }

    @Override
    public void applyTheme(ThemeData themedata) {
        super.applyTheme(themedata);
        applyThemeAutomatically(themedata);
    }
}
