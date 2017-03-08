package jp.hazuki.yuzubrowser.settings.preference.common;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;

public class IntentPreference extends Preference {
    public IntentPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IntentPreference);
        Intent intent = new Intent();
        intent.setClassName(getContext(), a.getString(R.styleable.IntentPreference_intent));
        setIntent(intent);
        a.recycle();
    }
}
