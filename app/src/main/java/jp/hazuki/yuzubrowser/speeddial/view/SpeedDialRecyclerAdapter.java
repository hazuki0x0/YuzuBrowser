package jp.hazuki.yuzubrowser.speeddial.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

/**
 * Created by hazuki on 17/02/20.
 */

class SpeedDialRecyclerAdapter extends ArrayRecyclerAdapter<SpeedDial, SpeedDialRecyclerAdapter.ViewHolder> {

    private ArrayList<SpeedDial> mData;

    public SpeedDialRecyclerAdapter(Context context, ArrayList<SpeedDial> list, OnRecyclerListener listener) {
        super(context, list, listener);
        mData = list;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, SpeedDial speedDial, int position) {
        holder.title.setText(speedDial.getTitle());
        holder.url.setText(speedDial.getUrl());
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_2
                , parent, false));
    }

    public void set(int pos, SpeedDial appData) {
        mData.set(pos, appData);
    }

    public SpeedDial get(int pos) {
        return mData.get(pos);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView url;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.text1);
            url = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
