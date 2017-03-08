package jp.hazuki.yuzubrowser.speeddial.view.appdata;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 16/12/10.
 */

public class AppDataListAdapter extends ArrayBaseAdapter<AppData> {

    public AppDataListAdapter(Context context, ArrayList<AppData> items) {
        super(context, items);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = getInflater().inflate(R.layout.dialog_avtive_app_item, viewGroup, false);

        AppData appData = getItem(i);

        TextView appName = (TextView) view.findViewById(R.id.appNameText);
        TextView packageName = (TextView) view.findViewById(R.id.packageNameText);
        ImageView icon = (ImageView) view.findViewById(R.id.iconImage);

        appName.setText(appData.getAppName());
        packageName.setText(appData.getPackageName());
        if (appData.getIcon() != null) {
            icon.setImageDrawable(appData.getIcon());
        } else {
            icon.setVisibility(View.GONE);
        }

        return view;
    }
}
