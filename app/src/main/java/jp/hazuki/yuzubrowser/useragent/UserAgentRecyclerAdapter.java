package jp.hazuki.yuzubrowser.useragent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

/**
 * Created by hazuki on 17/01/20.
 */

public class UserAgentRecyclerAdapter extends ArrayRecyclerAdapter<UserAgent, UserAgentRecyclerAdapter.ViewHolder> {

    private ArrayList<UserAgent> mData;

    public UserAgentRecyclerAdapter(Context context, ArrayList<UserAgent> list, OnRecyclerListener listener) {
        super(context, list, listener);
        mData = list;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, UserAgent userAgent, int position) {
        holder.title.setText(userAgent.name);
        holder.userAgent.setText(userAgent.useragent);
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_2
                , parent, false));
    }

    public void set(int pos, UserAgent appData) {
        mData.set(pos, appData);
    }

    public UserAgent getItem(int pos) {
        return mData.get(pos);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView userAgent;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.text1);
            userAgent = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
