package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;

final class FolderItemView extends LinearLayout {
    public FolderItemView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.bookmark_item_folder, this);
    }

    public void setTitle(CharSequence text) {
        ((TextView) findViewById(android.R.id.text1)).setText(text);
    }
}