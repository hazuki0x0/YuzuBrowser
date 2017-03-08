package jp.hazuki.yuzubrowser.menuwindow;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;

/**
 * Created by hazuki on 17/01/17.
 */

public class MenuWindow {

    private PopupWindow window;

    public MenuWindow(Context context, ActionList actionList, final ActionCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.drop_down_list, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.items);
        ActionNameArray array = new ActionNameArray(context);

        window = new PopupWindow(context);
        window.setContentView(v);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(context.getDrawable(R.drawable.menu_drop_down_background));
        window.setElevation(DisplayUtils.convertDpToPx(context, 10));

        TextView child;
        for (final Action action : actionList) {
            child = (TextView) inflater.inflate(R.layout.drop_down_list_item, null);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.run(action);
                    window.dismiss();
                }
            });
            child.setText(action.toString(array));
            layout.addView(child);
        }
    }

    public void show(View root) {
        window.showAtLocation(root, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    public void showAsDropDown(View anchor) {
        window.showAsDropDown(anchor);
    }

}
