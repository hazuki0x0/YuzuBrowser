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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.FontUtils;

public class MenuWindow implements PopupWindow.OnDismissListener {

    private PopupWindow window;
    private boolean locking = false;
    private Handler handler = new Handler();
    private OnMenuCloseListener mListener;

    public MenuWindow(Context context, ActionList actionList, final ActionCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ScrollView v = (ScrollView) inflater.inflate(R.layout.drop_down_list, null);
        LinearLayout layout = v.findViewById(R.id.items);
        ActionNameArray array = new ActionNameArray(context);

        window = new PopupWindow(context);
        window.setContentView(v);
        window.setOutsideTouchable(true);
        window.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(context.getDrawable(R.drawable.menu_drop_down_background));
        window.setElevation(DisplayUtils.convertDpToPx(context, 10));
        window.setOnDismissListener(this);
        window.getContentView().setFocusableInTouchMode(true);
        window.getContentView().setOnKeyListener((v1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_MENU &&
                    event.getRepeatCount() == 0 &&
                    event.getAction() == KeyEvent.ACTION_DOWN) {
                dismiss();
                return true;
            }
            return false;
        });

        TextView child;
        int fontSize = FontUtils.getTextSize(AppData.font_size.menu.get());
        for (final Action action : actionList) {
            child = (TextView) inflater.inflate(R.layout.drop_down_list_item, v, false);
            if (fontSize >= 0) {
                child.setTextSize(fontSize);
            }
            child.setOnClickListener(v12 -> {
                callback.run(action);
                window.dismiss();
            });
            child.setText(action.toString(array));
            layout.addView(child);
        }

        if (AppData.fullscreen.get())
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void show(View root, int gravity) {
        if (!locking) {
            //This is a magic!
            window.setFocusable(false);

            window.showAtLocation(root, gravity, 0, 0);

            //Reset focusable
            window.setFocusable(true);
        }
    }

    public void showAsDropDown(View anchor) {
        if (!locking) {
            //This is a magic!
            window.setFocusable(false);

            window.showAsDropDown(anchor);

            //Reset focusable
            window.setFocusable(true);
        }
    }

    public void setSystemUiVisibility(int flags) {
        window.getContentView().setSystemUiVisibility(flags);
    }

    public boolean isShowing() {
        return window.isShowing();
    }

    public void dismiss() {
        window.dismiss();
        locking = true;
        handler.postDelayed(lock, 50);
    }

    private Runnable lock = () -> locking = false;

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
