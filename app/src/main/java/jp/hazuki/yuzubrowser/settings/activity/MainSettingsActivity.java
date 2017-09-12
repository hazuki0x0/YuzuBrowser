package jp.hazuki.yuzubrowser.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.debug.DebugActivity;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class MainSettingsActivity extends ThemeActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MainSettingsFragment())
                    .commit();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Debug mode").setIntent(new Intent(this, DebugActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        if (caller instanceof YuzuPreferenceFragment) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,
                            PreferenceScreenFragment.newInstance(((YuzuPreferenceFragment) caller).getPreferenceResId(), pref.getKey()))
                    .addToBackStack(pref.getKey())
                    .commit();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int lightThemeResource() {
        return R.style.CustomThemeLight_Pref;
    }
}
