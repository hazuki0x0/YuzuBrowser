package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

public class IntContainer extends BaseContainer<Integer> {
    public IntContainer(String name, Integer def_value) {
        super(name, def_value);
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        mValue = shared_preference.getInt(mName, mDefValue);
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        editor.putInt(mName, mValue);
    }
}