package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.utils.Logger;

public class SpeedDialSettingActivity extends AppCompatActivity implements SpeedDialSettingActivityController, SpeedDialEditCallBack, FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        getFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SpeedDialSettingActivityFragment(), "main")
                    .commit();
        }

        shouldDisplayHomeUp();
    }

    @Override
    public boolean goBack() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void goEdit(SpeedDial speedDial) {
        getFragmentManager().beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, SpeedDialSettingActivityEditFragment.newInstance(speedDial))
                .commit();
    }

    @Override
    public void onEdited(SpeedDial speedDial) {
        goBack();
        Fragment fragment = getFragmentManager().findFragmentByTag("main");
        if (fragment instanceof SpeedDialEditCallBack) {
            ((SpeedDialEditCallBack) fragment).onEdited(speedDial);
        } else {
            Logger.d("fragment", "null");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void shouldDisplayHomeUp() {
        boolean canback = getFragmentManager().getBackStackEntryCount() == 0;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(canback);
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }
}
