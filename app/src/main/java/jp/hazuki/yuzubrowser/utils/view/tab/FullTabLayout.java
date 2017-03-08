package jp.hazuki.yuzubrowser.utils.view.tab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class FullTabLayout extends LinearLayout implements TabLayout {
    private final TabController mController = new TabController() {
        @Override
        public void requestAddView(View view, int index) {
            addView(view, index);
        }

        @Override
        public void requestRemoveViewAt(int id) {
            removeViewAt(id);
        }
    };

    public FullTabLayout(Context context) {
        this(context, null);
    }

    public FullTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    @Override
    public void setOnTabClickListener(OnTabClickListener l) {
        mController.setOnTabClickListener(l);
    }

    @Override
    public void addTabView(View view, LayoutParams params) {
        mController.addTabView(view);
        params.weight = 1.0f;
        addView(view, params);
    }

    @Override
    public void setCurrentTab(int id) {
        mController.setCurrentTab(id);
    }

    @Override
    public void removeTabAt(int id) {
        mController.removeTabAt(id);
    }

    @Override
    public void setSense(int sense) {
        mController.setSense(sense);
    }

    @Override
    public void fullScrollRight() {
        //do nothing
    }

    @Override
    public void fullScrollLeft() {
        //do nothing
    }

    @Override
    public void scrollToPosition(int position) {
        //do nothing
    }

    @Override
    public void swapTab(int a, int b) {
        mController.swapTab(a, b);
    }

    @Override
    public void moveTab(int from, int to, int new_curernt) {
        mController.moveTab(from, to, new_curernt);
    }
}
