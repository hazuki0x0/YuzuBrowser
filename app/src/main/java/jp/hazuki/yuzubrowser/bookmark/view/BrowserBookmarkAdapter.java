package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.TreeSet;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;

public class BrowserBookmarkAdapter extends ArrayAdapter<BookmarkItem> {
    private TreeSet<Integer> mSelected;

    public BrowserBookmarkAdapter(Context context, List<BookmarkItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BookmarkItem item = getItem(position);
        if (item instanceof BookmarkSite) {
            SiteItemView view;
            if (convertView == null || !(convertView instanceof SiteItemView)) {
                view = new SiteItemView(getContext());
            } else {
                view = (SiteItemView) convertView;
            }
            view.setTitle(item.title);
            view.setUrl(((BookmarkSite) item).url);

            checkSelected(view, position);

            return view;
        } else if (item instanceof BookmarkFolder) {
            FolderItemView view;
            if (convertView == null || !(convertView instanceof FolderItemView)) {
                view = new FolderItemView(getContext());
            } else {
                view = (FolderItemView) convertView;
            }
            view.setTitle(item.title);

            checkSelected(view, position);

            return view;
        } else {
            throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    public void setMultiSelectMode(boolean enable) {
        if (enable) {
            mSelected = new TreeSet<>();
        } else {
            mSelected = null;
            notifyDataSetChanged();
        }
    }

    public boolean isMultiSelectMode() {
        return mSelected != null;
    }

    public void select(int position) {
        if (mSelected == null)
            throw new IllegalStateException("not multi select mode");
        if (!mSelected.remove(position))
            mSelected.add(position);
        notifyDataSetChanged();
    }

    public void selectAll() {
        if (mSelected == null)
            throw new IllegalStateException("not multi select mode");
        if (isSelectAll())
            mSelected.clear();
        else {
            int count = getCount();
            for (int i = 0; i < count; ++i)
                mSelected.add(i);
        }
        notifyDataSetChanged();
    }

    public boolean isSelectAll() {
        return getCount() == mSelected.size();
    }

    private void checkSelected(View view, int position) {
        if (mSelected != null && mSelected.contains(position)) {
            view.setBackgroundColor(ResourcesCompat.getColor(getContext().getResources(), R.color.bookmark_multi_selected, getContext().getTheme()));
        } else {
            view.setBackgroundColor(0);
        }
    }

    public TreeSet<Integer> getSelectedList() {
        if (mSelected == null)
            throw new IllegalStateException("not multi select mode");
        return mSelected;
    }

    public int selectedCount() {
        if (mSelected == null)
            throw new IllegalStateException("not multi select mode");
        return mSelected.size();
    }
}
