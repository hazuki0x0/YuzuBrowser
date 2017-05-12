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

package jp.hazuki.yuzubrowser.gesture.multiFinger.detector;

import android.content.Context;
import android.view.MotionEvent;

public class MultiFingerGestureDetector {
    public static final int SWIPE_UNKNOWN = 0;
    public static final int SWIPE_UP = 1;
    public static final int SWIPE_DOWN = 2;
    public static final int SWIPE_LEFT = 3;
    public static final int SWIPE_RIGHT = 4;

    private boolean isTracking;
    private boolean showName;
    private int beforeDuration;
    private MfGestureAnalyzer analyzer;
    private MultiFingerGestureInfo info;
    private OnMultiFingerGestureListener gestureListener;

    public MultiFingerGestureDetector(Context context, OnMultiFingerGestureListener listener) {
        analyzer = new MfGestureAnalyzer(context);
        info = new MultiFingerGestureInfo();
        gestureListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean flag = false;
        switch (event.getAction() & 0xff) {
            case MotionEvent.ACTION_DOWN:
                startTracking(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                startTracking(event);
                break;
            case MotionEvent.ACTION_MOVE:
                moveTracking(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                flag = execute();
                stopTracking();
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                stopTracking();
                break;
        }
        return flag;
    }

    private boolean execute() {
        if (isTracking) {
            isTracking = false;
            return gestureListener.onGesturePerformed(info);
        }
        return false;
    }

    private void moveTracking(MotionEvent motionevent) {
        if (analyzer.getFingers() >= 1 && analyzer.isGesture(motionevent)) {
            MfGestureAnalyzer.GestureData gestureData = analyzer.getGesture(motionevent);
            if (gestureData.gestureFlag != 0) {
                if (beforeDuration != gestureData.gestureFlag) {
                    info.setFingers(gestureData.gesturePointer);
                    info.setTrace(gestureData.gestureFlag);
                    if (showName)
                        gestureListener.onShowGestureName(info);
                }
                beforeDuration = gestureData.gestureFlag;
            }
            analyzer.unTrackGesture();
            analyzer.trackGesture(motionevent);
        }
    }

    private void startTracking(MotionEvent event) {
        isTracking = true;
        info.clear();
        beforeDuration = 0;
        analyzer.trackGesture(event);
    }

    private void stopTracking() {
        isTracking = false;
        analyzer.unTrackGesture();
        gestureListener.onDismissGestureName();
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public void setSensitivity(int sensitivity) {
        analyzer.setSensitivity(sensitivity);
    }

    public interface OnMultiFingerGestureListener {
        boolean onGesturePerformed(MultiFingerGestureInfo info);

        void onShowGestureName(MultiFingerGestureInfo info);

        void onDismissGestureName();
    }
}
