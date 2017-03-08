package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

public interface Containable {
    void read(SharedPreferences shared_preference);

    void write(SharedPreferences.Editor editor);
}
