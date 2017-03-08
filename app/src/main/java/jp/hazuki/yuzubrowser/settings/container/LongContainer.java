package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

public class LongContainer extends BaseContainer<Long> {
    public LongContainer(String name, Long def_value) {
        super(name, def_value);
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        mValue = shared_preference.getLong(mName, mDefValue);
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        editor.putLong(mName, mValue);
    }
}
