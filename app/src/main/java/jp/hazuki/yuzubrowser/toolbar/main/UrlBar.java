package jp.hazuki.yuzubrowser.toolbar.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionArrayManager;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.toolbar.ButtonToolbarController;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager.RequestCallback;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.UrlUtils;
import jp.hazuki.yuzubrowser.utils.view.swipebutton.SwipeTextButton;

public class UrlBar extends ToolbarBase {
    private final ButtonToolbarController mLeftButtonController;
    private final ButtonToolbarController mRightButtonController;
    private final SwipeTextButton centerUrlButton;

    public UrlBar(Context context, ActionCallback action_callback, RequestCallback request_callback) {
        super(context, AppData.toolbar_url, R.layout.toolbar_url, request_callback);

        final int TOOLBAR_SIZE_Y = DisplayUtils.convertDpToPx(context, AppData.toolbar_url.size.get());

        SoftButtonActionManager softbtnManager = SoftButtonActionManager.getInstance(context);

        mLeftButtonController = new ButtonToolbarController((LinearLayout) findViewById(R.id.leftLinearLayout), action_callback, TOOLBAR_SIZE_Y);
        mRightButtonController = new ButtonToolbarController((LinearLayout) findViewById(R.id.rightLinearLayout), action_callback, TOOLBAR_SIZE_Y);

        centerUrlButton = (SwipeTextButton) findViewById(R.id.centerUrlButton);
        centerUrlButton.setActionData(softbtnManager.btn_url_center, action_callback);
        ButtonToolbarController.settingButtonSize(centerUrlButton, TOOLBAR_SIZE_Y);

        addButtons();
    }

    @Override
    public void onPreferenceReset() {
        super.onPreferenceReset();
        addButtons();

        centerUrlButton.notifyChangeState();
        centerUrlButton.setSense(AppData.swipebtn_sensitivity.get());
        centerUrlButton.setTextSize(AppData.toolbar_text_size_url.get());
    }

    @Override
    public void applyTheme(ThemeData themedata) {
        super.applyTheme(themedata);
        applyTheme(themedata, mLeftButtonController);
        applyTheme(themedata, mRightButtonController);
        applyTheme(themedata, centerUrlButton);
    }

    private void addButtons() {
        SoftButtonActionArrayManager manager = SoftButtonActionArrayManager.getInstance(getContext());
        mLeftButtonController.addButtons(manager.btn_url_left.list);
        mRightButtonController.addButtons(manager.btn_url_right.list);
        onThemeChanged(ThemeData.getInstance());// TODO
    }

    public void notifyChangeWebState(MainTabData data) {
        super.notifyChangeWebState(data);
        mLeftButtonController.notifyChangeState();
        mRightButtonController.notifyChangeState();
        centerUrlButton.notifyChangeState();

        if (data != null)
            changeTitle(data);
    }

    @Override
    public void resetToolBar() {
        mLeftButtonController.resetIcon();
        mRightButtonController.resetIcon();
    }

    public void changeTitle(final MainTabData data) {
        //need post Runnable?
        post(new Runnable() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void run() {
                if (data.getTitle() != null && !data.isInPageLoad()) {
                    centerUrlButton.setText(data.getTitle());
                    centerUrlButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                } else {
                    centerUrlButton.setText(UrlUtils.decodeUrl(data.getUrl()));
                    centerUrlButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }
            }
        });
    }

}
