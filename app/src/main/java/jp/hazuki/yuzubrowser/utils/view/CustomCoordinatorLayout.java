package jp.hazuki.yuzubrowser.utils.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by hazuki on 17/01/18.
 */

public class CustomCoordinatorLayout extends CoordinatorLayout {

    private int toolbarHeight = 0;

    public CustomCoordinatorLayout(Context context) {
        super(context);
    }

    public CustomCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setToolbarHeight(int toolbarHeight) {
        this.toolbarHeight = toolbarHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (toolbarHeight > 0) {
            if (ev.getY() > toolbarHeight) {
                return super.onTouchEvent(ev);
            } else {
                return false;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (toolbarHeight > 0) {
            if (ev.getY() > toolbarHeight) {
                return super.onInterceptTouchEvent(ev);
            } else {
                return false;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
