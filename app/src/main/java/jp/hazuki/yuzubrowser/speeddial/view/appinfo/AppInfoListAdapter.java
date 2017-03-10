package jp.hazuki.yuzubrowser.speeddial.view.appinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 16/12/03.
 */

public class AppInfoListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private ArrayList<AppInfo> arrayList;
    private final Context context;

    public AppInfoListAdapter(Context context, ArrayList<AppInfo> items) {
        arrayList = items;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = getInflater().inflate(R.layout.dialog_avtive_app_item, viewGroup, false);

        AppInfo appInfo = getItem(i);

        TextView appName = (TextView) view.findViewById(R.id.appNameText);
        TextView packageName = (TextView) view.findViewById(R.id.packageNameText);
        ImageView icon = (ImageView) view.findViewById(R.id.iconImage);

        appName.setText(appInfo.getAppName());
        packageName.setText(appInfo.getPackageName());
        if (appInfo.getIcon() != null) {
            icon.setImageDrawable(appInfo.getIcon());
        } else {
            icon.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public AppInfo getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int indexOf(AppInfo item) {
        return arrayList.indexOf(item);
    }

    public ArrayList<AppInfo> getItems() {
        return arrayList;
    }

    public void add(AppInfo item) {
        arrayList.add(item);
        notifyDataSetChanged();
    }

    public void remove(AppInfo item) {
        arrayList.remove(item);
        notifyDataSetChanged();
    }

    public void remove(int pos) {
        arrayList.remove(pos);
        notifyDataSetChanged();
    }

    public void set(int id, AppInfo item) {
        arrayList.set(id, item);
        notifyDataSetChanged();
    }

    public void replace(ArrayList<AppInfo> items) {
        if (items != null) {
            arrayList = items;
            notifyDataSetChanged();
        }
    }

    public int size() {
        return arrayList.size();
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }
}
