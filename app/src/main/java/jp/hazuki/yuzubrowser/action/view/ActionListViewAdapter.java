package jp.hazuki.yuzubrowser.action.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;

public class ActionListViewAdapter extends ArrayAdapter<Action> {
    private final ActionNameArray mActionNameArray;

    public ActionListViewAdapter(Context context, ActionList objects, ActionNameArray array) {
        super(context, 0, objects);
        mActionNameArray = (array != null) ? array : new ActionNameArray(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
        }
        Action action = getItem(position);
        if (action != null) {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(action.toString(mActionNameArray));
        }
        return convertView;
    }
}
