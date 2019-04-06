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

package jp.hazuki.yuzubrowser.browser.view;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomGestureOverlayView extends GestureOverlayView {

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
        return false;
    }

    boolean preDispatchTouchEvent(MotionEvent event) {
        if (listener != null) {
            boolean result = false;
            if (listener.onTouch(this, event)) {
                event.setAction(MotionEvent.ACTION_CANCEL);
                result = true;
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
}
