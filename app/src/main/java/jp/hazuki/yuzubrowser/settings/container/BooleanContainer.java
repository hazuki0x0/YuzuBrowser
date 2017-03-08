package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

public class BooleanContainer extends BaseContainer<Boolean> {
    public BooleanContainer(String name, Boolean def_value) {
        super(name, def_value);
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        mValue = shared_preference.getBoolean(mName, mDefValue);
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        editor.putBoolean(mName, mValue);
    }
}

