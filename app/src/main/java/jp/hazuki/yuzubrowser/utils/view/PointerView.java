/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.utils.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ResourceUtils;

public class PointerView extends RelativeLayout {
    //private static final String TAG = "PointerView";
    private final ImageView mCursor;
    private View mView;
    private float x = 0;
    private float y = 0;
    private boolean backFinish = true;
    private final MultiTouchGestureDetector mGestureDetector;

    private class MyMultiTouchGestureListener implements MultiTouchGestureDetector.OnMultiTouchGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mView == null)
                return false;

            final MotionEvent ev_down = MotionEvent.obtain(e.getDownTime(), e.getEventTime(), MotionEvent.ACTION_DOWN, x, y, 0);
            final MotionEvent ev_up = MotionEvent.obtain(e.getDownTime(), e.getEventTime(), MotionEvent.ACTION_UP, x, y, 0);

            mView.dispatchTouchEvent(ev_down);
            mView.dispatchTouchEvent(ev_up);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e2.getPointerCount() <= 1) {
                x -= distanceX;
                y -= distanceY;
                if (x < 0) {
                    x = 0;
                } else if (x > getMeasuredWidth()) {
                    x = getMeasuredWidth();
                }
                if (y < 0) {
                    y = 0;
                } else if (y > getMeasuredHeight()) {
                    y = getMeasuredHeight();
                }
                mCursor.layout((int) x, (int) y, (int) x + mCursor.getMeasuredWidth(), (int) y + mCursor.getMeasuredHeight());
            } else {
                if (mView == null)
                    return false;

                mView.dispatchTouchEvent(MotionEvent.obtain(e2.getDownTime(), e2.getEventTime(), MotionEvent.ACTION_MOVE, e2.getX(), e2.getY(), 0));
            }
            return false;
        }

        @Override
        public boolean onPointerUp(MotionEvent e) {
            if (mView == null)
                return false;

            if (e.getPointerCount() == 2)
                mView.dispatchTouchEvent(MotionEvent.obtain(e.getDownTime(), e.getEventTime(), MotionEvent.ACTION_UP, e.getX(), e.getY(), 0));
            return false;
        }

        @Override
        public boolean onPointerDown(MotionEvent e) {
            if (mView == null)
                return false;

            if (e.getPointerCount() == 2)
                mView.dispatchTouchEvent(MotionEvent.obtain(e.getDownTime(), e.getEventTime(), MotionEvent.ACTION_DOWN, e.getX(), e.getY(), 0));
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mView == null)
                return;

            final long time = SystemClock.uptimeMillis();
            final MotionEvent ev_down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, y, 0);
            mView.dispatchTouchEvent(ev_down);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public void onUp(MotionEvent e) {
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }

    public PointerView(Context context) {
        super(context);

        mCursor = new ImageView(context);
        addView(mCursor);

        mGestureDetector = new MultiTouchGestureDetector(context, new MyMultiTouchGestureListener());

        Bitmap bitmap = ResourceUtils.getBitmap(context, R.drawable.ic_mouse_cursor);
        Matrix matrix = new Matrix();
        float dimen = getResources().getDimension(R.dimen.dimen_cursor);
        matrix.postScale(dimen / bitmap.getHeight(), dimen / bitmap.getHeight());
        mCursor.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
        bitmap.recycle();
        mCursor.setVisibility(View.INVISIBLE);

        post(new Runnable() {
            @Override
            public void run() {
                x = getWidth() / 2;
                y = getHeight() / 2;
                requestLayout();
                mCursor.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mCursor.layout((int) x, (int) y, (int) x + mCursor.getMeasuredWidth(), (int) y + mCursor.getMeasuredHeight());
    }

    public void setView(View view) {
        mView = view;
    }

    public void setBackFinish(boolean b) {
        backFinish = b;
    }

    public boolean getBackFinish() {
        return backFinish;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
