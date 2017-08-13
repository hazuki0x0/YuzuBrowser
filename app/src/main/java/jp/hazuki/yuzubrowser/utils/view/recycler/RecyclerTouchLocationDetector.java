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

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;

public class RecyclerTouchLocationDetector implements RecyclerView.OnItemTouchListener {


    private RecyclerTouchLocation location;

    public RecyclerTouchLocationDetector() {
        location = RecyclerTouchLocation.NONE;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            int half = rv.getWidth() / 2;
            float x = e.getX();
            if (x <= half) {
                location = RecyclerTouchLocation.LEFT;
            } else {
                location = RecyclerTouchLocation.RIGHT;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @SuppressLint("RtlHardcoded")
    public int getGravity() {
        switch (location) {
            default:
            case LEFT:
                return Gravity.LEFT;
            case RIGHT:
                return Gravity.RIGHT;
        }
    }

    private enum RecyclerTouchLocation {
        NONE,
        LEFT,
        RIGHT
    }
}
