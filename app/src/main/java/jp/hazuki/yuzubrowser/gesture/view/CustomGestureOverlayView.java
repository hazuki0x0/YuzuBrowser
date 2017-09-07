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

package jp.hazuki.yuzubrowser.gesture.view;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomGestureOverlayView extends GestureOverlayView {

    private int top;
    private boolean touchInner;
    private boolean touchable;
    private OnTouchListener listener;

    public CustomGestureOverlayView(Context context) {
        super(context);
    }

    public CustomGestureOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomGestureOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        touchable = event.getRawY() > top;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            touchInner = touchable;
        }
        if (listener != null) {
            boolean result = false;
            if ((touchInner && action == MotionEvent.ACTION_MOVE && !touchable) || listener.onTouch(this, event)) {
                event.setAction(MotionEvent.ACTION_CANCEL);
                result = true;
                touchable = true;
            }

            result |= super.dispatchTouchEvent(event);

            return result;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        listener = l;
    }

    public void setWebFrame(AppBarLayout appBarLayout, View frame) {
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            Rect rect = new Rect();
            frame.getGlobalVisibleRect(rect);
            top = rect.top;
        });
    }

    @Override
    public boolean isEnabled() {
        return touchable && super.isEnabled();
    }
}
