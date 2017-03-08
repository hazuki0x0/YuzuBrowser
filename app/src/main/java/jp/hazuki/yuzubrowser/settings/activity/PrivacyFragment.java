package jp.hazuki.yuzubrowser.settings.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.PermissionUtils;

/**
 * Created by hazuki on 17/01/19.
 */

public class PrivacyFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        addPreferencesFromResource(R.xml.pref_privacy);

        findPreference("web_geolocation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PermissionUtils.checkLocation(getActivity())) {
                    return true;
                } else {
                    PermissionUtils.requestLocation(getActivity());
                    return false;
                }
            }
        });
    }
}
