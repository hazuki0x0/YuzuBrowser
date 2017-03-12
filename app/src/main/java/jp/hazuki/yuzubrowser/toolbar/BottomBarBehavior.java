package jp.hazuki.yuzubrowser.toolbar;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;


public class BottomBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    private int bottomBarHeight = -1;
    private int topBarHeight = -1;
    private boolean noTopBar;

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout bottomBar, View dependency) {
        if (dependency instanceof AppBarLayout) {
            View view = dependency.findViewById(R.id.topToolbarLayout);
            if (view != null) {
                topBarHeight = view.getHeight();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout bottomBar, View dependency) {
        if (dependency != null) {
            noTopBar = false;
            if (topBarHeight == -1) {
                topBarHeight = dependency.getHeight();
            }
            if (bottomBarHeight == -1 && bottomBar != null) {
                bottomBarHeight = bottomBar.getHeight();
            }

            if (topBarHeight != 0) {
                int height = -dependency.getTop() * bottomBarHeight / topBarHeight;

                ViewCompat.setTranslationY(bottomBar, Math.min(height, bottomBarHeight));
            } else {
                noTopBar = true;
            }
        } else {
            noTopBar = true;
        }

        return true;
    }

    public void setTopBarHeight(int height) {
        topBarHeight = height;
    }

    public boolean isNoTopBar() {
        return noTopBar;
    }
}
