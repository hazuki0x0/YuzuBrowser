package jp.hazuki.yuzubrowser.utils.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.utils.ArrayUtils;

public class ExtendedExpandableListView extends ExpandableListView {
    private OnChildLongClickListener mOnChildLongClickListener = null;
    private OnGroupLongClickListener mOnGroupLongClickListener = null;

    public ExtendedExpandableListView(Context context) {
        this(context, null);
    }

    public ExtendedExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showContextMenuForChild(v);
                return true;
            }
        });

        setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                if (mOnChildLongClickListener == null)
                    return;

                ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
                switch (ExpandableListView.getPackedPositionType(info.packedPosition)) {
                    case ExpandableListView.PACKED_POSITION_TYPE_CHILD: {
                        if (mOnChildLongClickListener == null)
                            break;
                        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

                        mOnChildLongClickListener.onChildLongClick(ExtendedExpandableListView.this, menu, info.targetView, groupPosition, childPosition, info.id);
                    }
                    break;
                    case ExpandableListView.PACKED_POSITION_TYPE_GROUP: {
                        if (mOnGroupLongClickListener == null)
                            break;
                        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);

                        mOnGroupLongClickListener.onGroupLongClick(ExtendedExpandableListView.this, menu, info.targetView, groupPosition, info.id);
                    }
                    break;
                }


            }
        });
    }

    public void setOnChildLongClickListener(OnChildLongClickListener l) {
        mOnChildLongClickListener = l;
    }

    public void setOnGroupLongClickListener(OnGroupLongClickListener l) {
        mOnGroupLongClickListener = l;
    }

    public interface OnChildLongClickListener {
        boolean onChildLongClick(ExpandableListView parent, ContextMenu menu, View v, int groupPosition, int childPosition, long id);
    }

    public interface OnGroupLongClickListener {
        boolean onGroupLongClick(ExpandableListView parent, ContextMenu menu, View v, int groupPosition, long id);
    }

    public boolean saveState(Bundle bundle) {
        ExpandableListAdapter adapter = getExpandableListAdapter();
        if (adapter == null) {
            return false;
        }

        bundle.putInt("firstVisiblePosition", getFirstVisiblePosition());

        int length = adapter.getGroupCount();
        ArrayList<Long> expandedList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (isGroupExpanded(i)) {
                expandedList.add(adapter.getGroupId(i));
            }
        }

        bundle.putLongArray("expandedList", ArrayUtils.toArray(expandedList));
        return true;
    }

    public boolean restoreState(Bundle bundle) {
        ExpandableListAdapter adapter = getExpandableListAdapter();
        if (adapter == null) {
            return false;
        }

        setSelection(bundle.getInt("firstVisiblePosition", 0));

        long expandedList[] = bundle.getLongArray("expandedList");
        int length = adapter.getGroupCount();
        for (int i = 0; i < length; i++) {
            long id = adapter.getGroupId(i);
            for (long l : expandedList) {
                if (l == id)
                    expandGroup(i);
            }
        }

        return true;
    }


}
