package jp.hazuki.yuzubrowser.settings.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;

/**
 * Created by hazuki on 17/01/16.
 */

public class BrowserSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        addPreferencesFromResource(R.xml.pref_browser_settings);
    }
}
