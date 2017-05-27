package jp.hazuki.yuzubrowser.toolbar.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.ToolbarActionManager;
import jp.hazuki.yuzubrowser.settings.container.ToolbarContainer;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.theme.ThemeData;
import jp.hazuki.yuzubrowser.toolbar.ButtonToolbarController;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager.RequestCallback;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.view.swipebutton.SwipeImageButton;

public class CustomToolbarBase extends ToolbarBase {
    private final ButtonToolbarController mButtonController;

    public CustomToolbarBase(Context context, ToolbarContainer toolbarContainer, ActionCallback action_callback, RequestCallback request_callback) {
        super(context, toolbarContainer, R.layout.toolbar_custom, request_callback);
        final int TOOLBAR_SIZE_Y = DisplayUtils.convertDpToPx(context, toolbarContainer.size.get());
        mButtonController = new ButtonToolbarController((LinearLayout) findViewById(R.id.linearLayout), action_callback, TOOLBAR_SIZE_Y) {
            private final LinearLayout.LayoutParams PARAMS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

            @Override
            public SwipeImageButton inflateButtonView(LayoutInflater inflater, ViewGroup parent) {
                View view = inflater.inflate(R.layout.toolbar_custom_button, null);
                parent.addView(view, PARAMS);
                return (SwipeImageButton) view.findViewById(R.id.button);
            }
        };
        addButtons();
    }

    @Override
    public void onPreferenceReset() {
        super.onPreferenceReset();
        addButtons();
    }

    @Override
    public void applyTheme(ThemeData themedata) {
        super.applyTheme(themedata);
        applyTheme(themedata, mButtonController);
    }

    private void addButtons() {
        mButtonController.addButtons(ToolbarActionManager.getInstance(getContext()).custombar1.list);
        onThemeChanged(ThemeData.getInstance());// TODO
    }

    public void notifyChangeWebState(MainTabData data) {
        super.notifyChangeWebState(data);
        mButtonController.notifyChangeState();
    }

    @Override
    public void resetToolBar() {
        mButtonController.resetIcon();
    }
}
