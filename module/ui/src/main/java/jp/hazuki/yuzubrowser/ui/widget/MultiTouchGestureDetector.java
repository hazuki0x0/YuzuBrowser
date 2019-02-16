/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.core.utility.common.listener.OnTouchEventListener;

public class MultiTouchGestureDetector implements OnTouchEventListener {
    public interface OnMultiTouchGestureListener {
        void onUp(MotionEvent e);

        boolean onDown(MotionEvent e);

        boolean onPointerDown(MotionEvent e);

        boolean onPointerUp(MotionEvent e);

        void onShowPress(MotionEvent e);

        boolean onSingleTapUp(MotionEvent e);

        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        void onLongPress(MotionEvent e);

        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }

    public interface OnMultiTouchDoubleTapListener {
        boolean onSingleTapConfirmed(MotionEvent e);

        boolean onDoubleTap(MotionEvent e);

        boolean onDoubleTapEvent(MotionEvent e);

        boolean onDoubleTapScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        boolean onDoubleTapFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }

    public static class SimpleOnGestureListener implements OnMultiTouchGestureListener, OnMultiTouchDoubleTapListener {
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        @Override
        public void onUp(MotionEvent e) {
        }

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onPointerDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onPointerUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapScroll(MotionEvent e1, MotionEvent e2,
                                         float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onDoubleTapFling(MotionEvent e1, MotionEvent e2,
                                        float velocityX, float velocityY) {
            return false;
        }
    }

    private int mTouchSlopSquare;
    private int mDoubleTapSlopSquare;
    private int mMaximumFlingVelocity;

    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    // constants for Message.what used by GestureHandler below
    private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;

    private final Handler mHandler;
    private final OnMultiTouchGestureListener mListener;
    private OnMultiTouchDoubleTapListener mDoubleTapListener;

    private boolean mStillDown;
    private boolean mInLongPress;
    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInBiggerTapRegion;

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;
    private MotionEvent mCurrentPointerDownEvent;

    private boolean mIsDoubleTapping;

    private float mLastMotionY;
    private float mLastMotionX;

    private boolean mIsLongpressEnabled;

    private VelocityTracker mVelocityTracker;

    private static class GestureHandler extends Handler {
        private final MultiTouchGestureDetector mDetector;

        GestureHandler(MultiTouchGestureDetector detector) {
            mDetector = detector;
        }

        GestureHandler(Handler handler, MultiTouchGestureDetector detector) {
            super(handler.getLooper());
            mDetector = detector;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PRESS:
                    mDetector.mListener.onShowPress(mDetector.mCurrentDownEvent);
                    break;

                case LONG_PRESS:
                    mDetector.dispatchLongPress();
                    break;

                case TAP:
                    // If the user's finger is still down, do not count it as a tap
                    if (mDetector.mDoubleTapListener != null && !mDetector.mStillDown) {
                        mDetector.mDoubleTapListener.onSingleTapConfirmed(mDetector.mCurrentDownEvent);
                    }
                    break;

                default:
                    throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }


    public MultiTouchGestureDetector(Context context, OnMultiTouchGestureListener listener) {
        this(context, listener, null);
    }


    public MultiTouchGestureDetector(Context context, OnMultiTouchGestureListener listener, Handler handler) {
        if (handler != null) {
            mHandler = new GestureHandler(handler, this);
        } else {
            mHandler = new GestureHandler(this);
        }
        mListener = listener;
        if (listener instanceof OnMultiTouchDoubleTapListener) {
            setOnDoubleTapListener((OnMultiTouchDoubleTapListener) listener);
        }
        init(context);
    }

    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        mIsLongpressEnabled = true;

        // Fallback to support pre-donuts releases
        int touchSlop, doubleTapSlop;
        if (context == null) {
            throw new NullPointerException("Context is null");
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }


    public void setOnDoubleTapListener(OnMultiTouchDoubleTapListener onDoubleTapListener) {
        mDoubleTapListener = onDoubleTapListener;
    }


    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        mIsLongpressEnabled = isLongpressEnabled;
    }


    public boolean isLongpressEnabled() {
        return mIsLongpressEnabled;
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        final int action = ev.getAction();
        final float y = ev.getY();
        final float x = ev.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        boolean handled = false;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (ev.getPointerCount() == 2) {
                    if (mCurrentPointerDownEvent != null) {
                        mCurrentPointerDownEvent.recycle();
                    }
                    mCurrentPointerDownEvent = MotionEvent.obtain(ev);
                }
                handled = mListener.onPointerDown(ev);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (!mIsDoubleTapping && !mInLongPress && !mAlwaysInTapRegion) {
                    // A fling must travel the minimum tap distance
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    final float velocityY = velocityTracker.getYVelocity();
                    final float velocityX = velocityTracker.getXVelocity();

                    mListener.onFling(mCurrentPointerDownEvent, ev, velocityX, velocityY);
                }

                // Ending a multitouch gesture and going back to 1 finger
                if (ev.getPointerCount() == 2) {
                    int index = (((action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT) == 0) ? 1 : 0;
                    mLastMotionX = ev.getX(index);
                    mLastMotionY = ev.getY(index);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = VelocityTracker.obtain();
                    }
                }
                handled = mListener.onPointerUp(ev);
                break;

            case MotionEvent.ACTION_DOWN:
                if (mDoubleTapListener != null) {
                    boolean hadTapMessage = mHandler.hasMessages(TAP);
                    if (hadTapMessage) mHandler.removeMessages(TAP);
                    if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null) && hadTapMessage &&
                            isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, ev)) {
                        // This is a second tap
                        mIsDoubleTapping = true;
                        // Give a callback with the first tap of the double-tap
                        handled = mDoubleTapListener.onDoubleTap(mCurrentDownEvent);
                        // Give a callback with down event of the double-tap
                        handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                    } else {
                        // This is a first tap
                        mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
                    }
                }

                mLastMotionX = x;
                mLastMotionY = y;
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(ev);
                if (mCurrentPointerDownEvent != null) {
                    mCurrentPointerDownEvent.recycle();
                    mCurrentPointerDownEvent = null;
                }
                mAlwaysInTapRegion = true;
                mAlwaysInBiggerTapRegion = true;
                mStillDown = true;
                mInLongPress = false;

                if (mIsLongpressEnabled) {
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendEmptyMessageAtTime(LONG_PRESS, mCurrentDownEvent.getDownTime()
                            + TAP_TIMEOUT + LONGPRESS_TIMEOUT);
                }
                mHandler.sendEmptyMessageAtTime(SHOW_PRESS, mCurrentDownEvent.getDownTime() + TAP_TIMEOUT);
                handled |= mListener.onDown(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mInLongPress) {
                    break;
                }
                final float scrollX = mLastMotionX - x;
                final float scrollY = mLastMotionY - y;
                if (mIsDoubleTapping) {
                    // Give the move events of the double-tap
                    handled = mDoubleTapListener.onDoubleTapEvent(ev);
                }
                if (mAlwaysInTapRegion) {
                    final int deltaX = (int) (x - mCurrentDownEvent.getX());
                    final int deltaY = (int) (y - mCurrentDownEvent.getY());
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > mTouchSlopSquare) {
                        if (mIsDoubleTapping)
                            handled = mDoubleTapListener.onDoubleTapScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                        else
                            handled = mListener.onScroll((ev.getPointerCount() > 1) ? mCurrentPointerDownEvent : mCurrentDownEvent, ev, scrollX, scrollY);
                        mLastMotionX = x;
                        mLastMotionY = y;
                        mAlwaysInTapRegion = false;
                        mHandler.removeMessages(TAP);
                        mHandler.removeMessages(SHOW_PRESS);
                        mHandler.removeMessages(LONG_PRESS);
                    }
                    int mBiggerTouchSlopSquare = 20 * 20;
                    if (distance > mBiggerTouchSlopSquare) {
                        mAlwaysInBiggerTapRegion = false;
                    }
                } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                    if (mIsDoubleTapping)
                        handled = mDoubleTapListener.onDoubleTapScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                    else
                        handled = mListener.onScroll((ev.getPointerCount() > 1) ? mCurrentPointerDownEvent : mCurrentDownEvent, ev, scrollX, scrollY);
                    mLastMotionX = x;
                    mLastMotionY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                mStillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);
                if (mIsDoubleTapping) {
                    // Finally, give the up event of the double-tap
                    handled = mDoubleTapListener.onDoubleTapEvent(ev);
                }
                if (mInLongPress) {
                    mHandler.removeMessages(TAP);
                    mInLongPress = false;
                } else if (mAlwaysInTapRegion) {
                    handled = mListener.onSingleTapUp(ev);
                } else {
                    // A fling must travel the minimum tap distance
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    final float velocityY = velocityTracker.getYVelocity();
                    final float velocityX = velocityTracker.getXVelocity();

                    //if ((Math.abs(velocityY) > mMinimumFlingVelocity)
                    //		|| (Math.abs(velocityX) > mMinimumFlingVelocity)){
                    if (mIsDoubleTapping)
                        handled = mDoubleTapListener.onDoubleTapFling(mCurrentDownEvent, ev, velocityX, velocityY);
                    else
                        handled = mListener.onFling(mCurrentDownEvent, ev, velocityX, velocityY);
                    //}
                }
                if (mPreviousUpEvent != null) {
                    mPreviousUpEvent.recycle();
                }
                // Hold the event we obtained above - listeners may have changed the original.
                mPreviousUpEvent = currentUpEvent;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mIsDoubleTapping = false;
                mHandler.removeMessages(SHOW_PRESS);
                mHandler.removeMessages(LONG_PRESS);
                mListener.onUp(ev);
                break;

            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
        }

        return handled;
    }

    private void cancel() {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mIsDoubleTapping = false;
        mStillDown = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
                                          MotionEvent secondDown) {
        if (!mAlwaysInBiggerTapRegion) {
            return false;
        }

        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }

    private void dispatchLongPress() {
        mHandler.removeMessages(TAP);
        mInLongPress = true;
        mListener.onLongPress(mCurrentDownEvent);
    }
}
