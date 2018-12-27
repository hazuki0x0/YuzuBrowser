package jp.hazuki.yuzubrowser.legacy.settings.preference.common;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.legacy.R;

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
