package jp.hazuki.yuzubrowser.toolbar.main;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;

import jp.hazuki.yuzubrowser.settings.container.ToolbarContainer;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.toolbar.AbstractToolbar;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager.RequestCallback;

public abstract class ToolbarBase extends AbstractToolbar {
    protected final ToolbarContainer mPreferences;
    protected final RequestCallback mRequestCallback;

    private ToolbarBase(Context context) {
        super(context);
        throw new IllegalAccessError();
    }

    public ToolbarBase(Context context, ToolbarContainer preference, int layout_id, RequestCallback request_callback) {
        super(context);
        mPreferences = preference;
        mRequestCallback = request_callback;
        LayoutInflater.from(context).inflate(layout_id, this);
    }

    public ToolbarContainer getToolbarPreferences() {
        return mPreferences;
    }

    public void onPreferenceReset() {
        setVisibility(mRequestCallback.shouldShowToolbar(mPreferences.visibility, null) ? VISIBLE : GONE);
    }

    public void onFullscreeenChanged(boolean isFullscreen) {
        setVisibility(mRequestCallback.shouldShowToolbar(mPreferences.visibility, null) ? VISIBLE : GONE);
    }

    public void notifyChangeWebState(MainTabData data) {
        if (data != null)
            setVisibility(mRequestCallback.shouldShowToolbar(mPreferences.visibility, data) ? VISIBLE : GONE);
    }

    public void toggleVisibility() {
        boolean isVisible = getVisibility() == VISIBLE;
        mPreferences.visibility.setAlwaysVisible(!isVisible);
        setVisibility(!isVisible ? VISIBLE : GONE);
    }

    public void onActivityConfigurationChanged(Configuration newConfig) {
        setVisibility(mRequestCallback.shouldShowToolbar(mPreferences.visibility, null, newConfig) ? VISIBLE : GONE);
    }
}
