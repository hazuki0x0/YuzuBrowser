package jp.hazuki.yuzubrowser.settings.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import jp.hazuki.yuzubrowser.BrowserActivity;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;

/**
 * Created by hazuki on 17/01/16.
 */

public class UiSettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        addPreferencesFromResource(R.xml.pref_ui_settings);

        findPreference("theme_setting").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                restart();
                return true;
            }
        });

        findPreference("restart").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restart();
                return true;
            }
        });
    }

    private void restart() {
        Intent intent = new Intent(getActivity(), BrowserActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent start = new Intent(getActivity(), BrowserActivity.class);
        start.setAction(BrowserActivity.ACTION_FINISH);
        start.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(start);
        manager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 800, pendingIntent);
    }
}
