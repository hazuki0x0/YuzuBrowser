package jp.hazuki.yuzubrowser.search;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.view.swipebutton.SwipeController;

public class SearchButton extends AppCompatImageButton implements SwipeController.OnChangeListener {
    private final SwipeController mSwipeController;
    private Callback mCallback;

    public SearchButton(Context context) {
        this(context, null);
    }

    public SearchButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSwipeController = new SwipeController(context);
        mSwipeController.setOnChangeListener(this);
    }

    public interface Callback {
        void forceOpenUrl();

        void forceSearchWord();

        void autoSearch();

        void recognizeSpeech();
    }

    public void setActionCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mSwipeController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onEventOutSide() {
        setImageResource(R.drawable.ic_search_white_24dp);
        return false;
    }

    @Override
    public boolean onEventCancel() {
        setImageResource(R.drawable.ic_search_white_24dp);
        return false;
    }

    @Override
    public boolean onEventActionUp(int whatNo) {
        setImageResource(R.drawable.ic_search_white_24dp);

        switch (whatNo) {
            case SwipeController.SWIPE_LEFT:
                mCallback.forceOpenUrl();
                break;
            case SwipeController.SWIPE_DOWN:
                mCallback.forceSearchWord();
                break;
            case SwipeController.SWIPE_PRESS:
                mCallback.autoSearch();
                break;
        }
        return false;
    }

    @Override
    public boolean onEventActionDown() {
        return false;
    }

    @Override
    public void onChangeState(int whatNo) {
        switch (whatNo) {
            case SwipeController.SWIPE_LEFT:
                setImageResource(R.drawable.ic_public_white_24dp);
                break;
            case SwipeController.SWIPE_DOWN:
                setImageResource(R.drawable.ic_mode_edit_white_24dp);
                break;
            default:
                setImageResource(R.drawable.ic_search_white_24dp);
                break;
        }
    }

    @Override
    public void onLongPress() {
        mCallback.recognizeSpeech();
    }

    public void setSense(int sense) {
        mSwipeController.setSense(sense);
    }
}
