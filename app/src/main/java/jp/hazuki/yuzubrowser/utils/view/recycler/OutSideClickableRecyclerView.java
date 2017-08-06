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

package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class OutSideClickableRecyclerView extends RecyclerView {

    public OutSideClickableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private OnOutSideClickListener listener;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP
                && findChildViewUnder(event.getX(), event.getY()) == null) {
            if (listener != null) {
                listener.onOutSideClick();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void setOnOutSideClickListener(OnOutSideClickListener listener) {
        this.listener = listener;
    }

    public interface OnOutSideClickListener {
        void onOutSideClick();
    }
}
