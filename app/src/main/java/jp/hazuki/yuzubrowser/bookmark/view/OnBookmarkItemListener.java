package jp.hazuki.yuzubrowser.bookmark.view;

import android.view.View;

import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

/**
 * Created by hazuki on 17/02/28.
 */

public interface OnBookmarkItemListener extends OnRecyclerListener {
    void onItemLongClick(View v, int index);
}
