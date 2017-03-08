package jp.hazuki.yuzubrowser.utils.view;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DateSorter;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class DateSortedExpandableListAdapter implements ExpandableListAdapter {
    private final Context mContext;
    private Cursor mCursor;
    private final DateSorter mDateSorter;
    private int mChildCount[];
    private int mGroupCount;
    private final int mGroupLayoutId;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mDataSetObservable.notifyChanged();
        }

        @Override
        public void onInvalidated() {
            mDataSetObservable.notifyInvalidated();
        }
    };

    public DateSortedExpandableListAdapter(Context context, Cursor cursor, int group_layout_id) {
        mContext = context;
        mDateSorter = new DateSorter(context);
        mCursor = cursor;
        mGroupLayoutId = group_layout_id;
        buildMap();
    }

    private void buildMap() {
        int childCount[] = new int[DateSorter.DAY_COUNT];//default value is 0 in array
        int groupCount = 0;
        int currentDateIndex = -1;

        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);

            if (mCursor.moveToFirst()) {
                do {
                    long time = getTime(mCursor);//long time
                    int index = mDateSorter.getIndex(time);
                    if (index > currentDateIndex) {
                        ++groupCount;
                        if (index == DateSorter.DAY_COUNT - 1) {
                            childCount[index] = mCursor.getCount() - mCursor.getPosition();
                            break;
                        }
                        currentDateIndex = index;
                    }
                    ++childCount[currentDateIndex];
                } while (mCursor.moveToNext());
            }
        }

        mChildCount = childCount;
        mGroupCount = groupCount;
    }

    protected abstract long getTime(Cursor cursor);

    protected abstract long getId(Cursor cursor);

    public Context getContext() {
        return mContext;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void swapCursor(Cursor cursor) {
        if (cursor == mCursor)
            return;
        if (mCursor != null) {
            mCursor.unregisterDataSetObserver(mDataSetObserver);
            mCursor.close();
        }
        mCursor = cursor;
        buildMap();
        if (cursor != null)
            notifyDataSetChanged();
        else
            notifyDataSetInvalidated();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public int getGroupCount() {
        return mGroupCount;
    }

    protected int groupPositionToBin(int groupPosition) {
        if (mGroupCount == DateSorter.DAY_COUNT || mGroupCount == 0) {
            return groupPosition;
        }
        int arrayPosition = -1;
        while (groupPosition > -1) {
            arrayPosition++;
            if (mChildCount[arrayPosition] != 0) {
                groupPosition--;
            }
        }
        return arrayPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildCount[groupPositionToBin(groupPosition)];
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    protected boolean moveCursorToChildPosition(int groupPosition, int childPosition) {
        if (mCursor.isClosed()) return false;

        groupPosition = groupPositionToBin(groupPosition);
        int index = childPosition;
        for (int i = 0; i < groupPosition; i++) {
            index += mChildCount[i];
        }
        return mCursor.moveToPosition(index);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if (moveCursorToChildPosition(groupPosition, childPosition)) {
            return getId(mCursor);
        }
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static final class GroupItemView extends LinearLayout {
        public GroupItemView(Context context, int id) {
            super(context);
            LayoutInflater.from(context).inflate(id, this);
        }

        public void setText(CharSequence text) {
            ((TextView) findViewById(android.R.id.text1)).setText(text);
        }
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupItemView view;
        if (convertView == null || !(convertView instanceof GroupItemView)) {
            view = new GroupItemView(mContext, mGroupLayoutId);
        } else {
            view = (GroupItemView) convertView;
        }
        view.setText(mDateSorter.getLabel(groupPositionToBin(groupPosition)));
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return getChildView(mCursor, groupPosition, childPosition, isLastChild, convertView, parent);
    }

    public abstract View getChildView(Cursor cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent);

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return mCursor.isClosed() || mCursor.getCount() == 0;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return childId;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return groupId;
    }

}
