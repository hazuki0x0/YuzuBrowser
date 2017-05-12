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
import android.os.SystemClock;
import android.view.MotionEvent;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;

class MfGestureAnalyzer {
    private int sensitivity;

    private double[] dX = new double[5];
    private double[] dY = new double[5];
    private double[] iX = new double[5];
    private double[] iY = new double[5];

    private long startTime;
    private int fingers = 0;

    MfGestureAnalyzer(Context context) {
        sensitivity = DisplayUtils.convertDpToPx(context, AppData.multi_finger_gesture_sensitivity.get());
    }

    private int getGestureFlag() {
        switch (fingers) {
            case 1:
                if (-dY[0] > Math.abs(dX[0]) * 2.0D) {
                    return MultiFingerGestureDetector.SWIPE_UP;
                }
                if (dY[0] > Math.abs(dX[0]) * 2.0D) {
                    return MultiFingerGestureDetector.SWIPE_DOWN;
                }
                if (-dX[0] > Math.abs(dY[0]) * 2.0D) {
                    return MultiFingerGestureDetector.SWIPE_LEFT;
                }
                if (dX[0] > Math.abs(dY[0]) * 2.0D) {
                    return MultiFingerGestureDetector.SWIPE_RIGHT;
                }
                break;

            case 2:
                if ((-dY[0] > Math.abs(dX[0]) * 2.0D) && (-dY[1] > Math.abs(dX[1]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_UP;
                }
                if ((dY[0] > Math.abs(dX[0]) * 2.0D) && (dY[1] > Math.abs(dX[1]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_DOWN;
                }
                if ((-dX[0] > Math.abs(dY[0]) * 2.0D) && (-dX[1] > Math.abs(dY[1]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_LEFT;
                }
                if ((dX[0] > Math.abs(dY[0]) * 2.0D) && (dX[1] > Math.abs(dY[1]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_RIGHT;
                }
                break;

            case 3:
                if ((-dY[0] > Math.abs(dX[0]) * 2.0D) && (-dY[1] > Math.abs(dX[1]) * 2.0D) && (-dY[2] > Math.abs(dX[2]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_UP;
                }
                if ((dY[0] > Math.abs(dX[0]) * 2.0D) && (dY[1] > Math.abs(dX[1]) * 2.0D) && (dY[2] > Math.abs(dX[2]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_DOWN;
                }
                if ((-dX[0] > Math.abs(dY[0]) * 2.0D) && (-dX[1] > Math.abs(dY[1]) * 2.0D) && (-dX[2] > Math.abs(dY[2]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_LEFT;
                }
                if ((dX[0] > Math.abs(dY[0]) * 2.0D) && (dX[1] > Math.abs(dY[1]) * 2.0D) && (dX[2] > Math.abs(dY[2]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_RIGHT;
                }
                break;

            case 4:
                if ((-dY[0] > Math.abs(dX[0]) * 2.0D) && (-dY[1] > Math.abs(dX[1]) * 2.0D) && (-dY[2] > Math.abs(dX[2]) * 2.0D) && (-dY[3] > Math.abs(dX[3]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_UP;
                }
                if ((dY[0] > Math.abs(dX[0]) * 2.0D) && (dY[1] > Math.abs(dX[1]) * 2.0D) && (dY[2] > Math.abs(dX[2]) * 2.0D) && (dY[3] > Math.abs(dX[3]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_DOWN;
                }
                if ((-dX[0] > Math.abs(dY[0]) * 2.0D) && (-dX[1] > Math.abs(dY[1]) * 2.0D) && (-dX[2] > Math.abs(dY[2]) * 2.0D) && (-dX[3] > Math.abs(dY[3]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_LEFT;
                }
                if ((dX[0] > Math.abs(dY[0]) * 2.0D) && (dX[1] > Math.abs(dY[1]) * 2.0D) && (dX[2] > Math.abs(dY[2]) * 2.0D) && (dX[3] > Math.abs(dY[3]) * 2.0D)) {
                    return MultiFingerGestureDetector.SWIPE_RIGHT;
                }
                break;
        }
        return MultiFingerGestureDetector.SWIPE_UNKNOWN;
    }

    GestureData getGesture(MotionEvent paramMotionEvent) {
        for (int i = 0; i < fingers; i++) {
            dX[i] = (paramMotionEvent.getX(i) - iX[i]);
            dY[i] = (paramMotionEvent.getY(i) - iY[i]);
        }

        GestureData type = new GestureData();
        type.gestureFlag = getGestureFlag();
        type.gestureDuration = SystemClock.uptimeMillis() - startTime;
        type.gesturePointer = fingers;
        return type;
    }

    boolean isGesture(MotionEvent paramMotionEvent) {
        for (int i = 0; i < fingers; i++) {
            if (Math.abs(paramMotionEvent.getX(i) - iX[i]) >= sensitivity ||
                    Math.abs(paramMotionEvent.getY(i) - iY[i]) >= sensitivity) {
                return true;
            }
        }
        return false;
    }

    void trackGesture(MotionEvent paramMotionEvent) {
        int pointers = paramMotionEvent.getPointerCount();
        if (pointers > 5)
            pointers = 5;

        for (int i = 0; pointers > i; i++) {
            iX[i] = paramMotionEvent.getX(i);
            iY[i] = paramMotionEvent.getY(i);
        }
        fingers = pointers;
        startTime = SystemClock.uptimeMillis();
    }

    void unTrackGesture() {
        fingers = 0;
    }

    int getFingers() {
        return fingers;
    }

    void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    static class GestureData {
        long gestureDuration;
        int gestureFlag;
        int gesturePointer;
    }
}
