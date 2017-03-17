/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.menuwindow;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
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

public class MenuWindow implements PopupWindow.OnDismissListener {

    private PopupWindow window;
    private boolean locking = false;
    private Handler handler = new Handler();
    private OnMenuCloseListener mListener;

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
        window.setOnDismissListener(this);
        window.getContentView().setFocusableInTouchMode(true);
        window.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU &&
                        event.getRepeatCount() == 0 &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

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
        if (!locking)
            window.showAtLocation(root, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    public void showAsDropDown(View anchor) {
        if (!locking)
            window.showAsDropDown(anchor);
    }

    public boolean isShowing() {
        return window.isShowing();
    }

    public void dismiss() {
        window.dismiss();
        locking = true;
        handler.postDelayed(lock, 50);
    }

    private Runnable lock = new Runnable() {
        @Override
        public void run() {
            locking = false;
        }
    };

    public void setListener(OnMenuCloseListener listener) {
        mListener = listener;
    }

    @Override
    public void onDismiss() {
        if (mListener != null)
            mListener.onMenuClose();
    }

    public interface OnMenuCloseListener {
        void onMenuClose();
    }
}
