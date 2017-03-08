package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by hazuki on 17/02/28.
 */

public class SimpleViewHolder extends RecyclerView.ViewHolder {
    public final TextView textView;

    public SimpleViewHolder(View itemView, int id) {
        super(itemView);
        textView = (TextView) itemView.findViewById(id);
    }
}
