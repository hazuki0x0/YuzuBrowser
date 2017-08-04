package jp.hazuki.yuzubrowser.utils.view.swipebutton;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile;
import jp.hazuki.yuzubrowser.theme.ThemeData;
import jp.hazuki.yuzubrowser.utils.UrlUtils;

public class SwipeTextButton extends AppCompatButton implements SwipeController.OnChangeListener {
    private final SwipeSoftButtonController mController;

    private CharSequence content = "";
    private CharSequence visibleText;

    private boolean typeUrl;

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

    public void setTypeUrl(boolean typeUrl) {
        this.typeUrl = typeUrl;
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

    @Override
    public void setText(CharSequence text, BufferType type) {
        content = text != null ? text : "";
        setContentDescription(content);
        updateVisibleText(getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int availWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        updateVisibleText(availWidth);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private CharSequence getTruncatedText(int availWidth) {
        if (typeUrl) {
            return UrlUtils.ellipsizeUrl(content, getPaint(), availWidth);
        } else {
            return TextUtils.ellipsize(content, getPaint(), availWidth, TextUtils.TruncateAt.END);
        }
    }

    private void updateVisibleText(int availWidth) {
        CharSequence newText = getTruncatedText(availWidth);

        if (!newText.equals(visibleText)) {
            visibleText = newText;

            super.setText(visibleText, BufferType.SPANNABLE);
        }
    }
}
