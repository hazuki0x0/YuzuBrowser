package jp.hazuki.yuzubrowser.history;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.view.DateSortedExpandableListAdapter;

public class BrowserHistoryAdapter extends DateSortedExpandableListAdapter {
    public BrowserHistoryAdapter(Context context, Cursor cursor) {
        super(context, cursor, R.layout.expandablelistview_group);
    }

    @Override
    protected long getTime(Cursor cursor) {
        return cursor.getLong(BrowserHistoryManager.COLUMN_TIME_INDEX);
    }

    @Override
    protected long getId(Cursor cursor) {
        return cursor.getLong(BrowserHistoryManager.COLUMN_TIME_INDEX);
    }

    private static final class ChildItemView extends LinearLayout {
        public ChildItemView(Context context, int id) {
            super(context);
            LayoutInflater.from(context).inflate(id, this);
        }

        public void setUrl(CharSequence text) {
            ((TextView) findViewById(android.R.id.text2)).setText(text);
        }

        public void setTitle(CharSequence text) {
            ((TextView) findViewById(android.R.id.text1)).setText(text);
        }

        public void setIcon(Bitmap bitmap) {
            ImageView iconImageView = (ImageView) findViewById(R.id.iconImageView);
            iconImageView.setImageBitmap(bitmap);
        }

        public String getUrl() {
            return ((TextView) findViewById(android.R.id.text2)).getText().toString();
        }

        public String getTitle() {
            return ((TextView) findViewById(android.R.id.text1)).getText().toString();
        }
    }

    @Override
    public View getChildView(Cursor cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildItemView view;
        if (convertView == null || !(convertView instanceof ChildItemView)) {
            view = new ChildItemView(getContext(), R.layout.history_item_site);
        } else {
            view = (ChildItemView) convertView;
        }
        if (!moveCursorToChildPosition(groupPosition, childPosition)) {
            return view;
        }
        view.setUrl(cursor.getString(BrowserHistoryManager.COLUMN_URL_INDEX));
        view.setTitle(cursor.getString(BrowserHistoryManager.COLUMN_TITLE_INDEX));
        view.setIcon(ImageUtils.convertToBitmap(cursor.getBlob(BrowserHistoryManager.COLUMN_FAVICON_INDEX)));
        return view;
    }

    public static String convertViewToUrl(View view) {
        if (view instanceof ChildItemView)
            return ((ChildItemView) view).getUrl();
        else
            return null;
    }

    public static String convertViewToTitle(View view) {
        if (view instanceof ChildItemView)
            return ((ChildItemView) view).getTitle();
        else
            return null;
    }
}
