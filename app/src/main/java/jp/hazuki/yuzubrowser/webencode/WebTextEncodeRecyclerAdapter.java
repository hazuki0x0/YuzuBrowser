package jp.hazuki.yuzubrowser.webencode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;
import jp.hazuki.yuzubrowser.utils.view.recycler.SimpleViewHolder;

/**
 * Created by hazuki on 17/01/20.
 */

public class WebTextEncodeRecyclerAdapter extends ArrayRecyclerAdapter<WebTextEncode, SimpleViewHolder> {

    private ArrayList<WebTextEncode> mData;

    public WebTextEncodeRecyclerAdapter(Context context, ArrayList<WebTextEncode> list, OnRecyclerListener listener) {
        super(context, list, listener);
        mData = list;
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, WebTextEncode encode, int position) {
        holder.textView.setText(encode.encoding);
    }

    @Override
    protected SimpleViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new SimpleViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false),
                android.R.id.text1);
    }

    public void set(int pos, WebTextEncode encode) {
        mData.set(pos, encode);
    }

    public WebTextEncode getItem(int pos) {
        return mData.get(pos);
    }
}
