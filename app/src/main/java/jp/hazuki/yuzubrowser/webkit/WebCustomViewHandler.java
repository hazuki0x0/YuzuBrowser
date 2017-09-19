package jp.hazuki.yuzubrowser.webkit;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

public class WebCustomViewHandler {
    private View mCustomView = null;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private static final FrameLayout.LayoutParams FULLSCREEN_LP = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    private final ViewGroup mFullscreenLayout;
    private static final int ADD_FLAGS = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    private int mOldOrientation, mOldFlag, oldUiVisibility;

    public WebCustomViewHandler(ViewGroup fullscreenLayout) {
        mFullscreenLayout = fullscreenLayout;
    }

    public boolean isCustomViewShowing() {
        return mCustomView != null;
    }

    public void showCustomView(Activity activity, View view, int orientation, WebChromeClient.CustomViewCallback callback) {
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        mOldOrientation = activity.getRequestedOrientation();
        activity.setRequestedOrientation(orientation);

        oldUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        activity.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        WindowManager.LayoutParams win_params = activity.getWindow().getAttributes();
        mOldFlag = win_params.flags;
        win_params.flags |= ADD_FLAGS;
        activity.getWindow().setAttributes(win_params);

        mFullscreenLayout.setBackgroundColor(Color.BLACK);
        mFullscreenLayout.setVisibility(View.VISIBLE);
        mFullscreenLayout.addView(view, FULLSCREEN_LP);
        mFullscreenLayout.bringToFront();


        mCustomView = view;
        mCustomViewCallback = callback;
    }

    public void hideCustomView(Activity activity) {
        if (mCustomView == null) return;

        activity.setRequestedOrientation(mOldOrientation);

        activity.getWindow().getDecorView()
                .setSystemUiVisibility(oldUiVisibility);

        WindowManager.LayoutParams win_params = activity.getWindow().getAttributes();
        win_params.flags = mOldFlag;
        activity.getWindow().setAttributes(win_params);

        mFullscreenLayout.setVisibility(View.GONE);
        mFullscreenLayout.removeView(mCustomView);


        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
    }
}
