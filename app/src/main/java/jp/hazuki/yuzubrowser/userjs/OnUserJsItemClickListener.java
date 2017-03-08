package jp.hazuki.yuzubrowser.userjs;

import android.view.View;

import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

/**
 * Created by hazuki on 17/02/28.
 */

public interface OnUserJsItemClickListener extends OnRecyclerListener {
    void onInfoButtonClick(View v, int index);

    void onCheckBoxClicked(View v, int index);
}
