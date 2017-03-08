package jp.hazuki.yuzubrowser.utils.view.tab;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class ScrollableTabLayout extends HorizontalScrollView implements TabLayout {
    private final TabController mController = new TabController() {
        @Override
        public void requestAddView(View view, int index) {
            mLayout.addView(view, index);
        }

        @Override
        public void requestRemoveViewAt(int id) {
            mLayout.removeViewAt(id);
        }
    };
    private final LinearLayout mLayout;

    private Handler handler = new Handler();

    public ScrollableTabLayout(Context context) {
        this(context, null);
    }

    public ScrollableTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        mLayout = new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(mLayout);
    }

    @Override
    public void setOnTabClickListener(OnTabClickListener l) {
        mController.setOnTabClickListener(l);
    }

    @Override
    public void addTabView(View view, LinearLayout.LayoutParams params) {
        mController.addTabView(view);
        mLayout.addView(view, params);
    }

    @Override
    public void setCurrentTab(int id) {
        mController.setCurrentTab(id);
    }

    @Override
    public void removeTabAt(final int id) {
        final int x = getScrollX();
        mController.removeTabAt(id);
        scrollTo(x, 0);
    }

    @Override
    public void setSense(int sense) {
        mController.setSense(sense);
    }

    private final Runnable mFullScrollRightRunnable = new Runnable() {
        @Override
        public void run() {
            fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        }
    };

    private final Runnable mFullScrollLeftRunnable = new Runnable() {
        @Override
        public void run() {
            fullScroll(HorizontalScrollView.FOCUS_LEFT);
        }
    };

    @Override
    public void fullScrollRight() {
        handler.post(mFullScrollRightRunnable);
    }

    @Override
    public void fullScrollLeft() {
        handler.post(mFullScrollLeftRunnable);
    }

    @Override
    public void scrollToPosition(final int position) {
        post(new Runnable() {
            @Override
            public void run() {
                int scrollX = getScrollX();
                View view = mLayout.getChildAt(position);
                int left = view.getLeft();
                int right = view.getRight();
                if (right <= scrollX || left >= getWidth() + scrollX)
                    smoothScrollTo(left, 0);
            }
        });
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
