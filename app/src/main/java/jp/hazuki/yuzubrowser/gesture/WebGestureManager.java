package jp.hazuki.yuzubrowser.gesture;

import android.content.Context;
import android.gesture.GestureOverlayView;

import jp.hazuki.yuzubrowser.settings.data.AppData;

public class WebGestureManager extends GestureManager {
    public WebGestureManager(Context context) {
        super(context, GESTURE_TYPE_WEB);
    }

    @Override
    public int getGestureStrokeType() {
        return GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE;
    }

    @Override
    public double getGestureScore() {
        return AppData.gesture_score_web.get();
    }
}
