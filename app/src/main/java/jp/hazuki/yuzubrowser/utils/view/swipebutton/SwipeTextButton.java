package jp.hazuki.yuzubrowser.utils.view.swipebutton;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile;
import jp.hazuki.yuzubrowser.theme.ThemeData;

public class SwipeTextButton extends AppCompatButton implements SwipeController.OnChangeListener {
    private final SwipeSoftButtonController mController;

    public SwipeTextButton(Context context) {
        this(context, null);
    }

    public SwipeTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mController = new SwipeSoftButtonController(getContext().getApplicationContext());
    }

    public void setActionData(SoftButtonActionFile action_list, ActionCallback callback) {
        mController.setActionData(action_list, callback);
        mController.setOnChangeListener(this);
    }

    public void notifyChangeState() {
        mController.notifyChangeState();
        setVisibility(mController.shouldShow() ? VISIBLE : GONE);
    }

    public void setSense(int sense) {
        mController.setSense(sense);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onLongPress() {
    }

    @Override
    public boolean onEventOutSide() {
        setBackgroundResource(R.drawable.swipebtn_image_background_normal);
        return false;
    }

    @Override
    public boolean onEventCancel() {
        setBackgroundResource(R.drawable.swipebtn_image_background_normal);
        return false;
    }

    @Override
    public boolean onEventActionUp(int whatNo) {
        setBackgroundResource(R.drawable.swipebtn_image_background_normal);
        return false;
    }

    @Override
    public boolean onEventActionDown() {
        if (ThemeData.isEnabled() && ThemeData.getInstance().toolbarButtonBackgroundPress != null)
            setBackground(ThemeData.getInstance().toolbarButtonBackgroundPress);
        else
            setBackgroundResource(R.drawable.swipebtn_image_background_pressed);
        return false;
    }

    @Override
    public void onChangeState(int whatNo) {
    }
}
