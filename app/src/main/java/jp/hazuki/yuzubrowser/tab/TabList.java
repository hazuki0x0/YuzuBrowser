package jp.hazuki.yuzubrowser.tab;

import android.os.Bundle;
import android.view.View;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class TabList implements Iterable<MainTabData> {
    private final List<MainTabData> mList = new LinkedList<>();
    private int mCurrentNo = -1;

    public MainTabData add(CustomWebView web, View view) {
        MainTabData tab = new MainTabData(web, view);
        mList.add(tab);
        return tab;
    }

    public void setCurrentTab(int no) {
        mCurrentNo = no;
    }

    public void remove(int no) {
        mList.remove(no);
    }

    public int move(int from, int to) {
        ArrayUtils.move(mList, from, to);

        if (from == mCurrentNo) {
            return mCurrentNo = to;
        } else {
            if (from <= mCurrentNo && to >= mCurrentNo) {
                return --mCurrentNo;
            } else if (from >= mCurrentNo && to <= mCurrentNo) {
                return ++mCurrentNo;
            }
        }
        return mCurrentNo;
    }

    public int indexOf(MainTabData tab) {
        return mList.indexOf(tab);
    }

    public int indexOf(CustomWebView web) {
        int i = 0;
        for (MainTabData data : mList) {
            if (data.mWebView == web) return i;
            ++i;
        }
        return -1;
    }

    public int size() {
        return mList.size();
    }

    public boolean isEmpty() {
        return mList.isEmpty();
    }

    public boolean isFirst() {
        return mCurrentNo == 0;
    }

    public boolean isLast() {
        return mCurrentNo == mList.size() - 1;
    }

    public boolean isFirst(int no) {
        return no == 0;
    }

    public boolean isLast(int no) {
        return no == mList.size() - 1;
    }

    public int getLastTabNo() {
        return mList.size() - 1;
    }

    public int getCurrentTabNo() {
        return mCurrentNo;
    }

    public void swap(int a, int b) {
        Collections.swap(mList, a, b);
    }

    public List<MainTabData> getList() {
        return mList;
    }

    @Override
    public Iterator<MainTabData> iterator() {
        return mList.iterator();
    }

    public MainTabData getCurrentTabData() {
        if (mCurrentNo < 0 || mCurrentNo >= mList.size()) return null;
        return mList.get(mCurrentNo);
    }

    public MainTabData get(int no) {
        if (no < 0 || no >= mList.size()) return null;
        return mList.get(no);
    }

    public MainTabData get(CustomWebView web) {
        for (MainTabData data : mList) {
            if (data.mWebView == web) return data;
        }
        return null;
    }

    public void destroy() {
        for (MainTabData data : mList) {
            data.mWebView.setEmbeddedTitleBarMethod(null);
            data.mWebView.destroy();
        }
        mList.clear();
        mCurrentNo = -1;
    }

    public void clearCache(boolean includeDiskFiles) {
        for (MainTabData data : mList) {
            data.mWebView.clearCache(includeDiskFiles);
        }
    }

    private static final String EXTRA_CURRENT_NO = "TAB.E0";
    private static final String EXTRA_LIST_COUNT = "TAB.E1";
    private static final String EXTRA_TAB_TYPE = "TAB.E2";

    public void saveInstanceState(Bundle bundle) {
        int i = 0;
        //Bundle[] bundle_list = new Bundle[mList.size()];
        int[] types = new int[mList.size()];
        for (MainTabData web : mList) {
            Bundle state = new Bundle();
            web.mWebView.saveState(state);
            types[i] = web.getTabType();
            bundle.putBundle("TAB.W" + String.valueOf(i), state);
            ++i;
        }
        bundle.putInt(EXTRA_LIST_COUNT, mList.size());
        bundle.putInt(EXTRA_CURRENT_NO, mCurrentNo);
        bundle.putSerializable(EXTRA_TAB_TYPE, types);
    }

    public RestoreStateData restoreInstanceState(Bundle bundle) {
        int currentNo = bundle.getInt(EXTRA_CURRENT_NO);
        int list_count = bundle.getInt(EXTRA_LIST_COUNT);
        int[] tabType = (int[]) bundle.getSerializable(EXTRA_TAB_TYPE);
        Bundle[] list = new Bundle[list_count];
        for (int i = 0; i < list_count; ++i) {
            list[i] = bundle.getBundle("TAB.W" + String.valueOf(i));
        }
        return new RestoreStateData(currentNo, list, tabType);
    }
}
