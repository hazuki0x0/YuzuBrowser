package jp.hazuki.yuzubrowser.toolbar.main;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ProgressBar;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.toolbar.ButtonToolbarController;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager.RequestCallback;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;

public class ProgressToolBar extends ToolbarBase {
    private final ProgressBar progressBar;

    public ProgressToolBar(Context context, ActionCallback action_callback, RequestCallback request_callback) {
        super(context, AppData.toolbar_progress, R.layout.toolbar_progress, request_callback);

        int TOOLBAR_SIZE_Y = DisplayUtils.convertDpToPx(context, AppData.toolbar_progress.size.get());

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ButtonToolbarController.settingButtonSizeHeight(progressBar, TOOLBAR_SIZE_Y);
    }

    @Override
    public void onPreferenceReset() {
        super.onPreferenceReset();
    }

    @Override
    public void applyTheme(ThemeData themedata) {
        super.applyTheme(themedata);
        if (themedata != null && themedata.progressIndeterminateColor != 0)
            progressBar.getIndeterminateDrawable().setColorFilter(themedata.progressIndeterminateColor, PorterDuff.Mode.SRC_ATOP);
        else
            progressBar.getIndeterminateDrawable().setColorFilter(null);

        if (themedata != null && themedata.progressColor != 0)
            progressBar.getProgressDrawable().setColorFilter(themedata.progressColor, PorterDuff.Mode.SRC_ATOP);
        else
            progressBar.getProgressDrawable().setColorFilter(null);
    }

    public void notifyChangeWebState(MainTabData data) {
        super.notifyChangeWebState(data);
        if (data != null)
            changeProgress(data);
    }

    public void changeProgress(MainTabData data) {
        int p = data.mProgress;
        boolean inload = data.isInPageLoad();

        if (p == 100 || !inload) {
            progressBar.setVisibility(View.INVISIBLE);
            if (progressBar.isIndeterminate()) progressBar.setIndeterminate(false);//need this?
        } else if (p <= 0) {
            progressBar.setVisibility(View.VISIBLE);
            if (!progressBar.isIndeterminate()) progressBar.setIndeterminate(true);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            if (progressBar.isIndeterminate()) progressBar.setIndeterminate(false);
            progressBar.setProgress(p);
        }
    }
}
