package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.view.View;


public interface OnRecyclerListener {
    void onRecyclerItemClicked(View v, int position);

    boolean onRecyclerItemLongClicked(View v, int position);
}
