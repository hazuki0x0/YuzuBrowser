package jp.hazuki.yuzubrowser.utils.view.swipebutton;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeController {
    public static final int SWIPE_CANCEL = 0;
    public static final int SWIPE_RIGHT = 0x01;
    public static final int SWIPE_LEFT = 0x02;
    public static final int SWIPE_UP = 0x04;
    public static final int SWIPE_DOWN = 0x08;
    public static final int SWIPE_X_Y = SWIPE_RIGHT | SWIPE_LEFT | SWIPE_UP | SWIPE_DOWN;
    public static final int SWIPE_PRESS = 0x10;
    public static final int SWIPE_LPRESS = 0x20;

    private int mWhatNo = SWIPE_PRESS;
    private int mSense;

    private final GestureDetector mDetector;

    public interface OnChangeListener {
        boolean onEventOutSide();

        boolean onEventCancel();

        boolean onEventActionUp(int whatNo);

        boolean onEventActionDown();

        void onChangeState(int whatNo);

        void onLongPress();
    }

    public void setOnChangeListener(OnChangeListener l) {
        mListener = l;
    }

    private OnChangeListener mListener = null;

    public SwipeController(Context context) {
        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                int rangeX = (int) (e2.getRawX() - e1.getRawX());
                int rangeY = (int) (e2.getRawY() - e1.getRawY());

                int check = 0;
                if (rangeX > mSense)
                    check |= SWIPE_RIGHT;
                else if (rangeX < -mSense)
                    check |= SWIPE_LEFT;
                if (rangeY > mSense)
                    check |= SWIPE_DOWN;
                else if (rangeY < -mSense)
                    check |= SWIPE_UP;

                if (check == 0) {
                    if (mWhatNo != SWIPE_CANCEL) {
                        mWhatNo = SWIPE_CANCEL;
                        if (mListener != null) mListener.onChangeState(SWIPE_CANCEL);
                    }
                } else {
                    switch (check & SWIPE_X_Y) {
                        case SWIPE_RIGHT:
                            callListener(SWIPE_RIGHT);
                            break;
                        case SWIPE_LEFT:
                            callListener(SWIPE_LEFT);
                            break;
                        case SWIPE_DOWN:
                            callListener(SWIPE_DOWN);
                            break;
                        case SWIPE_UP:
                            callListener(SWIPE_UP);
                            break;
                        case SWIPE_RIGHT | SWIPE_DOWN:
                            callListener((rangeX > rangeY) ? SWIPE_RIGHT : SWIPE_DOWN);
                            break;
                        case SWIPE_RIGHT | SWIPE_UP:
                            callListener((rangeX > -rangeY) ? SWIPE_RIGHT : SWIPE_UP);
                            break;
                        case SWIPE_LEFT | SWIPE_DOWN:
                            callListener((-rangeX > rangeY) ? SWIPE_LEFT : SWIPE_DOWN);
                            break;
                        case SWIPE_LEFT | SWIPE_UP:
                            callListener((-rangeX > -rangeY) ? SWIPE_LEFT : SWIPE_UP);
                            break;
                    }
                }

                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            private void callListener(int what) {
                if (mWhatNo != what) {
                    mWhatNo = what;
                    if (mListener != null)
                        mListener.onChangeState(what);
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mWhatNo == SWIPE_PRESS) {
                    mWhatNo = SWIPE_LPRESS;
                    onEventLongPress();
                    if (mListener != null) {
                        mListener.onChangeState(mWhatNo);
                        mListener.onLongPress();
                    }
                }
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onEventActionDown();
                if (mListener != null) return mListener.onEventActionDown();
                break;
            case MotionEvent.ACTION_UP:
                int tmp_no = mWhatNo;
                mWhatNo = SWIPE_PRESS;
                boolean ret = false;
                onEventActionUp(tmp_no);
                if (mListener != null) ret = mListener.onEventActionUp(tmp_no);
                return ret;
            case MotionEvent.ACTION_CANCEL:
                onEventCancel();
                if (mListener != null) return mListener.onEventCancel();
                break;
            case MotionEvent.ACTION_OUTSIDE:
                onEventOutSide();
                if (mListener != null) return mListener.onEventOutSide();
                break;
        }
        return false;
    }

    public void notifyChangeState() {
        if (mListener != null) mListener.onChangeState(mWhatNo);
    }

    public void setToDefault() {
        mWhatNo = SWIPE_PRESS;
        mListener.onChangeState(mWhatNo);
    }

    public void setSense(int sense) {
        mSense = sense;
    }

    protected int getCurrentWhatNo() {
        return mWhatNo;
    }

    protected void onEventActionDown() {
    }

    protected void onEventActionUp(int whatNo) {
    }

    protected void onEventLongPress() {
    }

    protected void onEventCancel() {
    }

    protected void onEventOutSide() {
    }
}
