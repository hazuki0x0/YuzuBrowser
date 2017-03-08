package jp.hazuki.yuzubrowser.utils.view.tab;

import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public interface TabLayout {
    interface OnTabClickListener {
        void onTabDoubleClick(int id);

        void onTabChangeClick(int from, int to);

        void onTabLongClick(int id);

        boolean onTabTouch(View v, MotionEvent ev, int id, boolean selected);

        void onChangeCurrentTab(int from, int to);

        void onTabSwipeUp(int id);

        void onTabSwipeDown(int id);
    }

    void setOnTabClickListener(OnTabClickListener l);

    void addTabView(View view, LinearLayout.LayoutParams params);

    void setCurrentTab(int id);

    void removeTabAt(int id);

    void setSense(int sense);

    void fullScrollRight();

    void fullScrollLeft();

    void scrollToPosition(int position);

    void swapTab(int a, int b);

    void moveTab(int from, int to, int new_curernt);
}
