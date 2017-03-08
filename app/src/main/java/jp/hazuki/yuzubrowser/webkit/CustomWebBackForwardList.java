package jp.hazuki.yuzubrowser.webkit;

import android.webkit.WebBackForwardList;

import java.util.ArrayList;

public class CustomWebBackForwardList extends ArrayList<CustomWebHistoryItem> {
    private static final long serialVersionUID = 6556040641241028726L;
    private final int mCurrent;

    public CustomWebBackForwardList(int current) {
        super();
        mCurrent = current;
    }

    public CustomWebBackForwardList(WebBackForwardList list) {
        super(list.getSize());
        mCurrent = list.getCurrentIndex();

        int size = list.getSize();
        for (int i = 0; i < size; ++i) {
            add(new CustomWebHistoryItem(list.getItemAtIndex(i)));
        }
    }

    public CustomWebBackForwardList(int current, int capacity) {
        super(capacity);
        mCurrent = current;
    }

    public int getCurrent() {
        return mCurrent;
    }

    public CustomWebHistoryItem getNext() {
        return getBackOrForward(1);
    }

    public CustomWebHistoryItem getPrev() {
        return getBackOrForward(-1);
    }

    public CustomWebHistoryItem getBackOrForward(int dist) {
        int next = mCurrent + dist;
        if (next >= 0 && next < size())
            return get(next);
        else
            return null;
    }
}
