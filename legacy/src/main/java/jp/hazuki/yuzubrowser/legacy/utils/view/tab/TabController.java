/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.utils.view.tab;

import android.graphics.Color;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.legacy.utils.graphics.DividerDrawable;
import jp.hazuki.yuzubrowser.legacy.utils.view.tab.TabLayout.OnTabClickListener;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;
import jp.hazuki.yuzubrowser.ui.theme.ThemeData;

public abstract class TabController {
    private int mCurrentId = 0;
    private OnTabClickListener mListener = null;
    private final List<View> mViewList = new ArrayList<>();
    private int mSense;

    public void setOnTabClickListener(OnTabClickListener l) {
        mListener = l;
    }

    public void addTabView(View view) {
        settingTab(view, mViewList.size());
        mViewList.add(view);
    }

    public void addTabView(int id, View view) {
        mViewList.add(id, view);
        if (mCurrentId >= id) {
            mCurrentId++;
        }
        int count = mViewList.size();
        for (int i = 0; i < count; ++i) {
            settingTab(mViewList.get(i), i);
        }
    }

    public abstract void requestAddView(View view, int index);

    public void setCurrentTab(int id) {
        mListener.onChangeCurrentTab((mCurrentId < mViewList.size()) ? mCurrentId : -1, id);
        mCurrentId = id;
    }

    public void removeTabAt(int id) {
        View view = mViewList.get(id);
        view.setOnClickListener(null);
        view.setOnLongClickListener(null);
        view.setOnTouchListener(null);
        mViewList.remove(id);

        int count = mViewList.size();
        for (int i = 0; i < count; ++i) {
            settingTab(mViewList.get(i), i);
        }

        requestRemoveViewAt(id);
        if (mCurrentId > id) {
            mCurrentId--;
        }
    }

    public abstract void requestRemoveViewAt(int id);

    public void settingTab(final View viewholder, final int tabId) {
        viewholder.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCurrentId == tabId) {
                            mListener.onTabDoubleClick(tabId);
                        } else {
                            mListener.onTabChangeClick(mCurrentId, tabId);
                        }
                    }
                }
        );
        viewholder.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onTabLongClick(tabId);
                        return true;
                    }
                }
        );
        viewholder.setOnTouchListener(new View.OnTouchListener() {
            private boolean isActionAccepted = false;
            private GestureDetector detector = new GestureDetector(viewholder.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (!isActionAccepted) {
                        if (e1 == null || e2 == null)
                            return true;
                        int rangeY = (int) (e2.getRawY() - e1.getRawY());

                        if (rangeY > mSense) {
                            isActionAccepted = true;
                            mListener.onTabSwipeDown(tabId);
                        }
                        if (rangeY < -mSense) {
                            isActionAccepted = true;
                            mListener.onTabSwipeUp(tabId);
                        }
                    }
                    return false;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_UP:
                        isActionAccepted = false;
                        break;
                }
                detector.onTouchEvent(ev);
                return mListener.onTabTouch(viewholder, ev, tabId, tabId == mCurrentId);
            }
        });
    }

    public void setSense(int sense) {
        mSense = sense;
    }

    public void swapTab(int a, int b) {
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        mCurrentId = (a == mCurrentId) ? b : (b == mCurrentId) ? a : mCurrentId;
        View va = mViewList.get(a);
        va.setOnClickListener(null);
        va.setOnLongClickListener(null);
        va.setOnTouchListener(null);
        View vb = mViewList.get(b);
        vb.setOnClickListener(null);
        vb.setOnLongClickListener(null);
        vb.setOnTouchListener(null);
        Collections.swap(mViewList, a, b);
        requestRemoveViewAt(b);
        requestRemoveViewAt(a);
        requestAddView(vb, a);
        requestAddView(va, b);
        settingTab(va, b);
        settingTab(vb, a);
    }

    public void moveTab(int from, int to, int new_curernt) {
        View view = mViewList.remove(from);
        mViewList.add(to, view);
        requestRemoveViewAt(from);
        requestAddView(view, to);

        mCurrentId = new_curernt;

        int count = mViewList.size();
        for (int i = 0; i < count; ++i) {
            settingTab(mViewList.get(i), i);
        }
    }

    public abstract DividerDrawable newDividerInstance();

    public void applyTheme(LinearLayout layout, ThemeData themedata) {
        DividerDrawable divider = (DividerDrawable) layout.getDividerDrawable();
        if (isShowDivider(AppPrefs.show_tab_divider.get(), themedata == null || themedata.showTabDivider)) {
            if (divider == null) {
                divider = newDividerInstance();
                layout.setDividerDrawable(divider);
            }
        } else {
            layout.setDividerDrawable(null);
            return;
        }

        if (themedata != null && themedata.tabDividerColor != 0) {
            divider.setColor(themedata.tabDividerColor);
        } else if (themedata != null && themedata.toolbarTextColor != 0) {
            divider.setWithTextColor(themedata.toolbarTextColor);
        } else {
            divider.setWithTextColor(Color.WHITE);
        }
    }

    private boolean isShowDivider(int mode, boolean show) {
        return mode == 1 || mode == 0 && show;
    }
}
