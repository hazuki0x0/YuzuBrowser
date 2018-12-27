package jp.hazuki.yuzubrowser.legacy.settings.container;

import android.content.SharedPreferences;

public class StringContainer extends BaseContainer<String> {
    public StringContainer(String name, String def_value) {
        super(name, def_value);
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        mValue = shared_preference.getString(mName, mDefValue);
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        editor.putString(mName, mValue);
    }
}

