package jp.hazuki.yuzubrowser.toolbar;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/01/18.
 */

public class BottomBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    private int bottomBarHeight = -1;
    private int topBarHeight = -1;

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

        if (topBarHeight == -1) {
            topBarHeight = dependency.getHeight();
        }
        if (bottomBarHeight == -1) {
            bottomBarHeight = bottomBar.getHeight();
        }

        int height = -dependency.getTop() * bottomBarHeight / topBarHeight;

        ViewCompat.setTranslationY(bottomBar, Math.min(height, bottomBarHeight));
        return true;
    }

    public void setTopBarHeight(int height) {
        topBarHeight = height;
    }
}
