package jp.hazuki.yuzubrowser.toolbar;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.settings.container.ToolbarContainer;
import jp.hazuki.yuzubrowser.settings.container.ToolbarVisibilityContainter;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.toolbar.main.CustomToolbar;
import jp.hazuki.yuzubrowser.toolbar.main.ProgressToolBar;
import jp.hazuki.yuzubrowser.toolbar.main.TabBar;
import jp.hazuki.yuzubrowser.toolbar.main.ToolbarBase;
import jp.hazuki.yuzubrowser.toolbar.main.UrlBar;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.view.tab.TabLayout;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class ToolbarManager {
    private final LinearLayout topToolbarLayout, bottomToolbarLayout, leftToolbarLayout, rightToolbarLayout;
    private final LinearLayout webToolbarLayout, fixedWebToolbarLayout;
    private final LinearLayout topToolbarAlwaysLayout, bottomToolbarAlwaysLayout;
    private final View findOnPage;
    private boolean mIsWebToolbarCombined = false;
    private final TabBar tabBar;
    private final UrlBar urlBar;
    private final ProgressToolBar progressBar;
    private final CustomToolbar customBar;
    private BottomBarBehavior bottomBarBehavior;
    private int bottomBarHeight = -1;

    public static final int LOCATION_UNDEFINED = -1;
    public static final int LOCATION_TOP = 0;
    public static final int LOCATION_BOTTOM = 1;
    public static final int LOCATION_WEB = 2;
    public static final int LOCATION_FIXED_WEB = 3;
    public static final int LOCATION_FLOAT_BOTTOM = 4;
    public static final int LOCATION_LEFT = 5;
    public static final int LOCATION_RIGHT = 6;
    public static final int LOCATION_TOP_ALWAYS = 7;
    public static final int LOCATION_BOTTOM_ALWAYS = 8;

    private static final LinearLayout.LayoutParams TOOLBAR_LAYOUT_PARAMS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    public interface RequestCallback {
        boolean shouldShowToolbar(ToolbarVisibilityContainter visibility, MainTabData tabdata);

        boolean shouldShowToolbar(ToolbarVisibilityContainter visibility, MainTabData tabdata, Configuration newConfig);
    }

    public ToolbarManager(Activity activity, ActionCallback action_callback, RequestCallback request_callback) {
        topToolbarLayout = (LinearLayout) activity.findViewById(R.id.topToolbarLayout);
        bottomToolbarLayout = (LinearLayout) activity.findViewById(R.id.bottomToolbarLayout);
        topToolbarAlwaysLayout = (LinearLayout) activity.findViewById(R.id.topAlwaysToolbarLayout);
        bottomToolbarAlwaysLayout = (LinearLayout) activity.findViewById(R.id.bottomAlwaysToolbarLayout);
        leftToolbarLayout = (LinearLayout) activity.findViewById(R.id.leftToolbarLayout);
        rightToolbarLayout = (LinearLayout) activity.findViewById(R.id.rightToolbarLayout);
        findOnPage = activity.findViewById(R.id.find);

        webToolbarLayout = new LinearLayout(activity);
        webToolbarLayout.setOrientation(LinearLayout.VERTICAL);
        fixedWebToolbarLayout = new LinearLayout(activity);
        fixedWebToolbarLayout.setOrientation(LinearLayout.VERTICAL);
        fixedWebToolbarLayout.setBackgroundColor(0xFF000000);

        tabBar = new TabBar(activity, action_callback, request_callback);
        urlBar = new UrlBar(activity, action_callback, request_callback);
        progressBar = new ProgressToolBar(activity, action_callback, request_callback);
        customBar = new CustomToolbar(activity, action_callback, request_callback);

        Object o = bottomToolbarLayout.getLayoutParams();
        if (o instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) o).getBehavior();
            if (behavior instanceof BottomBarBehavior) {
                bottomBarBehavior = (BottomBarBehavior) behavior;
            }
        }

    }

    public View getFindOnPage() {
        return findOnPage;
    }

    public TabBar getTabBar() {
        return tabBar;
    }

    public UrlBar getUrlBar() {
        return urlBar;
    }

    public ProgressToolBar getProgressToolBar() {
        return progressBar;
    }

    public CustomToolbar getCustomBar() {
        return customBar;
    }

    public LinearLayout getBottomToolbarAlwaysLayout() {
        return bottomToolbarAlwaysLayout;
    }

    public void addToolbarView(Resources res) {
        addToolbarView(res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public void addToolbarView(boolean isPortrait) {
        for (int i = 1; i <= 4; ++i) {
            addSingleToolbarView(i, tabBar, isPortrait);
            addSingleToolbarView(i, urlBar, isPortrait);
            addSingleToolbarView(i, progressBar, isPortrait);
            addSingleToolbarView(i, customBar, isPortrait);
        }
    }

    private void addSingleToolbarView(int priority, ToolbarBase toolbar, boolean isPortrait) {
        ToolbarContainer toolbarPreference = toolbar.getToolbarPreferences();
        int toolbarPriority;
        if (isPortrait) {
            toolbarPriority = toolbarPreference.location_priority.get();
        } else {
            toolbarPriority = toolbarPreference.location_landscape_priority.get();
            if (toolbarPriority < 0)
                toolbarPriority = toolbarPreference.location_priority.get();
        }
        if (toolbarPriority != priority)
            return;

        int location;
        if (isPortrait) {
            location = toolbarPreference.location.get();
        } else {
            location = toolbarPreference.location_landscape.get();
            if (location == LOCATION_UNDEFINED)
                location = toolbarPreference.location.get();
        }

        switch (location) {
            case LOCATION_TOP:
                topToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_BOTTOM:
            case LOCATION_FLOAT_BOTTOM:
                bottomToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_WEB:
                webToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_FIXED_WEB:
                fixedWebToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_LEFT:
                ((LinearLayout) toolbar.findViewById(R.id.linearLayout)).setOrientation(LinearLayout.VERTICAL);
                leftToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_RIGHT:
                ((LinearLayout) toolbar.findViewById(R.id.linearLayout)).setOrientation(LinearLayout.VERTICAL);
                rightToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_TOP_ALWAYS:
                topToolbarAlwaysLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            case LOCATION_BOTTOM_ALWAYS:
                bottomToolbarAlwaysLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS);
                break;
            default:
                throw new IllegalStateException("Unknown location:" + toolbar.getToolbarPreferences().location.get());
        }
    }

    public void changeCurrentTab(int to_id, MainTabData from, MainTabData to) {
        tabBar.changeCurrentTab(to_id);

        if (from != null) from.mWebView.setEmbeddedTitleBarMethod(null);
        setWebViewTitlebar(to.mWebView, !to.isInPageLoad());

        notifyChangeWebState(to);
    }

    public void swapTab(int a, int b) {
        tabBar.swapTab(a, b);
    }

    public void moveTab(int from, int to, int new_curernt) {
        tabBar.moveTab(from, to, new_curernt);
    }

    public void onPreferenceReset() {
        tabBar.onPreferenceReset();
        urlBar.onPreferenceReset();
        progressBar.onPreferenceReset();
        customBar.onPreferenceReset();

        bottomToolbarLayout.getBackground().setAlpha(AppData.overlay_bottom_alpha.get());
    }

    public void onThemeChanged(ThemeData themedata) {
        tabBar.onThemeChanged(themedata);
        urlBar.onThemeChanged(themedata);
        progressBar.onThemeChanged(themedata);
        customBar.onThemeChanged(themedata);

        if (themedata != null && themedata.toolbarBackgroundColor != 0) {
            topToolbarLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            bottomToolbarLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            webToolbarLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            fixedWebToolbarLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            leftToolbarLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            rightToolbarLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            topToolbarAlwaysLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
            bottomToolbarAlwaysLayout.setBackgroundColor(themedata.toolbarBackgroundColor);
        } else {
            topToolbarLayout.setBackgroundResource(R.color.deep_gray);
            bottomToolbarLayout.setBackgroundResource(R.color.deep_gray);
            webToolbarLayout.setBackgroundResource(R.color.deep_gray);
            fixedWebToolbarLayout.setBackgroundResource(R.color.deep_gray);
            leftToolbarLayout.setBackgroundResource(R.color.deep_gray);
            rightToolbarLayout.setBackgroundResource(R.color.deep_gray);
            topToolbarAlwaysLayout.setBackgroundResource(R.color.deep_gray);
            bottomToolbarAlwaysLayout.setBackgroundResource(R.color.deep_gray);
        }

        bottomToolbarLayout.getBackground().setAlpha(AppData.overlay_bottom_alpha.get());

        for (int i = 0; i < bottomToolbarLayout.getChildCount(); ++i) {
            View view = bottomToolbarLayout.getChildAt(i);
            if (view instanceof SubToolbar)
                ((SubToolbar) view).applyTheme(themedata);
        }
    }

    public void onActivityConfigurationChanged(Configuration newConfig) {
        onActivityConfigurationChangedSingle(tabBar, newConfig);
        onActivityConfigurationChangedSingle(urlBar, newConfig);
        onActivityConfigurationChangedSingle(progressBar, newConfig);
        onActivityConfigurationChangedSingle(customBar, newConfig);

        addToolbarView(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    private void onActivityConfigurationChangedSingle(ToolbarBase toolbar, Configuration newConfig) {
        ViewGroup parent = (ViewGroup) toolbar.getParent();
        if (parent == null)
            throw new NullPointerException();
        if (parent == leftToolbarLayout || parent == rightToolbarLayout)
            ((LinearLayout) toolbar.findViewById(R.id.linearLayout)).setOrientation(LinearLayout.HORIZONTAL);
        parent.removeView(toolbar);
        toolbar.onActivityConfigurationChanged(newConfig);
    }

    public void onFullscreeenChanged(boolean isFullscreen) {
        tabBar.onFullscreeenChanged(isFullscreen);
        urlBar.onFullscreeenChanged(isFullscreen);
        progressBar.onFullscreeenChanged(isFullscreen);
        customBar.onFullscreeenChanged(isFullscreen);
    }

    public void onImeChanged(boolean shown) {
        if (shown) {
            bottomToolbarLayout.setVisibility(View.GONE);
            bottomToolbarAlwaysLayout.setVisibility(View.GONE);
        } else {
            bottomToolbarLayout.setVisibility(View.VISIBLE);
            bottomToolbarAlwaysLayout.setVisibility(View.VISIBLE);
        }
    }

    public void notifyChangeProgress(MainTabData data) {
        progressBar.changeProgress(data);
    }

    public void notifyChangeWebState() {
        notifyChangeWebState(null);
    }

    public void notifyChangeWebState(MainTabData data) {
        if (data != null)
            setWebViewTitlebar(data.mWebView, !data.isInPageLoad());

        tabBar.notifyChangeWebState(data);
        urlBar.notifyChangeWebState(data);
        progressBar.notifyChangeWebState(data);
        customBar.notifyChangeWebState(data);
    }

    public void resetToolBar() {
        tabBar.resetToolBar();
        urlBar.resetToolBar();
        customBar.resetToolBar();
    }

    public View addNewTabView() {
        return tabBar.addNewTabView();
    }

    public void scrollTabRight() {
        tabBar.fullScrollRight();
    }

    public void scrollTabLeft() {
        tabBar.fullScrollLeft();
    }

    public void scrollTabTo(int position) {
        tabBar.scrollToPosition(position);
    }

    public void removeTab(int no) {
        tabBar.removeTab(no);
    }

    public void setWebViewTitlebar(CustomWebView web, boolean combine) {
        if (fixedWebToolbarLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) fixedWebToolbarLayout.getParent()).removeView(fixedWebToolbarLayout);
        }
        if (combine) {
            if (!mIsWebToolbarCombined) {
                topToolbarLayout.removeView(webToolbarLayout);
                fixedWebToolbarLayout.addView(webToolbarLayout, 0);
            }
            web.setEmbeddedTitleBarMethod(fixedWebToolbarLayout);
        } else {
            if (mIsWebToolbarCombined) {
                fixedWebToolbarLayout.removeView(webToolbarLayout);
                topToolbarLayout.addView(webToolbarLayout);
            }
            web.setEmbeddedTitleBarMethod(fixedWebToolbarLayout);
        }
        mIsWebToolbarCombined = combine;
    }

    public boolean isWebToolbarCombined() {
        return mIsWebToolbarCombined;
    }

    public void setOnTabClickListener(TabLayout.OnTabClickListener l) {
        tabBar.setOnTabClickListener(l);
    }

    public void showGeolocationPrmissionPrompt(View view) {
        bottomToolbarLayout.addView(view, 0);
    }

    public void hideGeolocationPrmissionPrompt(View view) {
        bottomToolbarLayout.removeView(view);
    }

    public boolean isContainsWebToolbar(MotionEvent ev) {
        Rect rc = new Rect();
        fixedWebToolbarLayout.getGlobalVisibleRect(rc);
        return rc.contains((int) ev.getRawX(), (int) ev.getRawY());
    }

    public void onWebViewScroll(CustomWebView web, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (bottomBarBehavior.isNoTopBar()) {
            if (bottomBarHeight < 0) {
                bottomBarHeight = bottomToolbarLayout.getHeight();
            }

            float translationY = bottomToolbarLayout.getTranslationY();
            float newTrans;

            if (distanceY < 0) {
                //down
                newTrans = Math.max(0, distanceY + translationY);

            } else if (distanceY > 0) {
                //up
                newTrans = Math.min(bottomBarHeight, distanceY + translationY);
            } else {
                return;
            }

            Logger.d("trans", newTrans);

            ViewCompat.setTranslationY(bottomToolbarLayout, newTrans);

        }
    }
}
