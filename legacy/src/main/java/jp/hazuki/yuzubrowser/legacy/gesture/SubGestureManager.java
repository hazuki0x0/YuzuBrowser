package jp.hazuki.yuzubrowser.legacy.gesture;

import android.content.Context;
import android.gesture.GestureOverlayView;

import jp.hazuki.yuzubrowser.legacy.settings.data.AppData;

public class SubGestureManager extends GestureManager {
    public SubGestureManager(Context context) {
        super(context, GESTURE_TYPE_SUB);
    }

    @Override
    public int getGestureStrokeType() {
        return GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE;
    }

    @Override
    public double getGestureScore() {
        return AppData.gesture_score_sub.get();
    }
}
