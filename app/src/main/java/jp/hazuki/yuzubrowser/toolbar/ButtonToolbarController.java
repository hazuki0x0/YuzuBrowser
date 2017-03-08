package jp.hazuki.yuzubrowser.toolbar;

import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.view.swipebutton.SwipeImageButton;

public class ButtonToolbarController {
    private final int TOOLBAR_SIZE_Y;
    private final ViewGroup linearLayout;
    private final ActionCallback mActionCallback;
    private SwipeImageButton mButtonList[];

    public ButtonToolbarController(ViewGroup layout, ActionCallback action_callback, int toolbar_size) {
        linearLayout = layout;
        mActionCallback = action_callback;
        TOOLBAR_SIZE_Y = toolbar_size;
    }

    public void addButtons(List<SoftButtonActionFile> list) {
        int size = list.size();
        if (mButtonList != null) {// initialized
            if (mButtonList.length == size) {
                int i = 0;
                for (SwipeImageButton btn : mButtonList) {
                    btn.setActionData(list.get(i), mActionCallback);
                    btn.setSense(AppData.swipebtn_sensitivity.get());
                    ++i;
                }
            } else {
                linearLayout.removeAllViews();

                mButtonList = new SwipeImageButton[size];
                LayoutInflater inflater = LayoutInflater.from(linearLayout.getContext());

                for (int i = 0; i < size; ++i) {
                    SwipeImageButton btn = inflateButtonView(inflater, linearLayout);
                    mButtonList[i] = btn;
                    btn.setActionData(list.get(i), mActionCallback);
                    btn.setSense(AppData.swipebtn_sensitivity.get());
                    settingButtonSize(btn, TOOLBAR_SIZE_Y);
                }
            }
        } else {
            mButtonList = new SwipeImageButton[size];
            LayoutInflater inflater = LayoutInflater.from(linearLayout.getContext());

            for (int i = 0; i < size; ++i) {
                SwipeImageButton btn = inflateButtonView(inflater, linearLayout);
                mButtonList[i] = btn;
                settingButtonSize(btn, TOOLBAR_SIZE_Y);
            }
        }
    }

    public SwipeImageButton inflateButtonView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.toolbar_custom_button, null);
        parent.addView(view);
        return (SwipeImageButton) view.findViewById(R.id.button);
    }

    public static void settingButtonSize(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        params.width = size;
        view.setLayoutParams(params);
    }

    public static void settingButtonSizeHeight(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        view.setLayoutParams(params);
    }

    public void notifyChangeState() {
        for (SwipeImageButton btn : mButtonList) {
            btn.notifyChangeState();
        }
    }

    public void setColorFilter(ColorFilter cf) {
        for (SwipeImageButton btn : mButtonList)
            btn.setColorFilter(cf);
    }

    public void setBackgroundDrawable(Drawable background) {
        for (SwipeImageButton btn : mButtonList)
            btn.setImageDrawable(background);
    }
}
