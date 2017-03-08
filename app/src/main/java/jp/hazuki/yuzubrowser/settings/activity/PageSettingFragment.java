package jp.hazuki.yuzubrowser.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.webencode.WebTextEncodeSettingActivity;

/**
 * Created by hazuki on 17/01/16.
 */

public class PageSettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        addPreferencesFromResource(R.xml.pref_page_settings);

        findPreference("web_encode_list").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), WebTextEncodeSettingActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
