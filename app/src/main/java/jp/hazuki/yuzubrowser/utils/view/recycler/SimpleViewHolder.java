package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.view.View;
import android.widget.TextView;

public class SimpleViewHolder<T> extends ArrayRecyclerAdapter.ArrayViewHolder<T> {
    public final TextView textView;

    public SimpleViewHolder(View itemView, int id, ArrayRecyclerAdapter<T, ?> adapter) {
        super(itemView, adapter);
        textView = itemView.findViewById(id);
    }
}
