package jp.hazuki.yuzubrowser.history;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

public class BrowserHistoryAdapter extends ArrayRecyclerAdapter<BrowserHistory, BrowserHistoryAdapter.HistoryHolder> {

    private DateFormat dateFormat;
    private BrowserHistoryManager mManager;
    private OnHistoryItemListener mListener;
    private String mQuery;

    public static BrowserHistoryAdapter create(Context context, BrowserHistoryManager manager, OnHistoryItemListener listener) {
        List<BrowserHistory> histories = manager.getList(0, 100);
        return new BrowserHistoryAdapter(context, manager, histories, null, listener);
    }

    public static BrowserHistoryAdapter create(Context context, BrowserHistoryManager manager, String query, OnHistoryItemListener listener) {
        List<BrowserHistory> histories = manager.search(query, 0, 100);
        return new BrowserHistoryAdapter(context, manager, histories, query, listener);
    }

    public BrowserHistoryAdapter reCreate(Context context) {
        return create(context, mManager, mListener);
    }

    private BrowserHistoryAdapter(Context context, BrowserHistoryManager manager, List<BrowserHistory> list, String query, OnHistoryItemListener listener) {
        super(context, list, listener);
        dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        mManager = manager;
        mListener = listener;
        mQuery = query;
    }

    @Override
    public void onBindViewHolder(HistoryHolder holder, BrowserHistory item, final int position) {
        String url = Uri.parse(item.getUrl()).getHost();
        String time = dateFormat.format(new Date(item.getTime()));
        Bitmap image = mManager.getFavicon(item.getId());

        if (image == null) {
            holder.imageView.setImageResource(R.drawable.ic_public_white_24dp);
        } else {
            holder.imageView.setImageBitmap(image);
        }
        holder.titleTextView.setText(item.getTitle());
        holder.urlTextView.setText(url);
        holder.timeTextView.setText(time);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mListener.onItemLongClick(v, position);
            }
        });
    }

    @Override
    protected HistoryHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new HistoryHolder(inflater.inflate(R.layout.history_item, parent, false));
    }

    public void loadMore() {
        if (mQuery == null) {
            getItems().addAll(mManager.getList(getItemCount(), 100));
        } else {
            getItems().addAll(mManager.search(mQuery, getItemCount(), 100));
        }
    }

    class HistoryHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleTextView;
        TextView urlTextView;
        TextView timeTextView;

        public HistoryHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            urlTextView = (TextView) itemView.findViewById(R.id.urlTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        }
    }

    public interface OnHistoryItemListener extends OnRecyclerListener {
        boolean onItemLongClick(View v, int position);
    }
}
