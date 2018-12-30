package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.gesture.GestureListActivity;
import jp.hazuki.yuzubrowser.legacy.gesture.GestureManager;

public class GestureListPreference extends Preference {
    public GestureListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GestureListPreference);
        String title = a.getString(R.styleable.GestureListPreference_intentTitle);
        int gestureId = a.getInt(R.styleable.GestureListPreference_gestureId, -1);

        if (gestureId < 0)
            throw new IllegalArgumentException("gestureId is empty or negative : " + gestureId);

        Intent intent = new Intent(getContext(), GestureListActivity.class);
        intent.putExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, gestureId);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        setIntent(intent);
        a.recycle();
    }
}
