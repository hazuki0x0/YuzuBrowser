package jp.hazuki.yuzubrowser.tab;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/01/20.
 */

public class TabListRecyclerAdapter extends RecyclerView.Adapter<TabListRecyclerAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private TabList tabList;
    private OnRecyclerListener mListener;

    public TabListRecyclerAdapter(Context context, TabList list, OnRecyclerListener listener) {
        mInflater = LayoutInflater.from(context);
        tabList = list;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.tab_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // データ表示
        MainTabData tabData = tabList.get(position);
        if (tabData != null) {
            holder.title.setText(tabData.mTitle);
            holder.url.setText(tabData.mUrl);
        }

        // クリック処理
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRecyclerItemClicked(v, holder.getAdapterPosition());
            }
        });

        holder.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCloseButtonClicked(v, holder.getAdapterPosition());
            }
        });

        holder.historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHistoryButtonClicked(v, holder.getAdapterPosition());
            }
        });

        if (position == tabList.getCurrentTabNo())
            holder.itemView.setBackgroundResource(R.drawable.tab_list_item_background_selected);
        else
            holder.itemView.setBackgroundResource(R.drawable.tab_list_item_background);
    }

    @Override
    public int getItemCount() {
        if (tabList != null) {
            return tabList.size();
        } else {
            return 0;
        }
    }

    public void deleteItem(int pos) {
        tabList.remove(pos);
        notifyDataSetChanged();
    }

    public MainTabData getItem(int pos) {
        return tabList.get(pos);
    }

    public TabList getItemList() {
        return tabList;
    }

    public interface OnRecyclerListener {
        void onRecyclerItemClicked(View v, int position);

        void onCloseButtonClicked(View v, int position);

        void onHistoryButtonClicked(View v, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView url;
        View closeButton;
        View historyButton;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titleTextView);
            url = (TextView) itemView.findViewById(R.id.urlTextView);
            closeButton = itemView.findViewById(R.id.closeImageButton);
            historyButton = itemView.findViewById(R.id.tabHistoryImageButton);
        }
    }
}
