package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;

final class SiteItemView extends LinearLayout {
    public SiteItemView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.bookmark_item_site, this);
    }

    public void setUrl(CharSequence text) {
        ((TextView) findViewById(android.R.id.text2)).setText(text);
    }

    public void setTitle(CharSequence text) {
        ((TextView) findViewById(android.R.id.text1)).setText(text);
    }
}