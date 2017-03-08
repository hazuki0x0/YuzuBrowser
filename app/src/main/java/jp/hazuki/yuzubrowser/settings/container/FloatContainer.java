package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

public class FloatContainer extends BaseContainer<Float> {
    public FloatContainer(String name, Float def_value) {
        super(name, def_value);
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        mValue = shared_preference.getFloat(mName, mDefValue);
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        editor.putFloat(mName, mValue);
    }
}
