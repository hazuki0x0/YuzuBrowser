package jp.hazuki.yuzubrowser.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import jp.hazuki.yuzubrowser.Constants;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;

public class BrowserHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(DisplayUtils.convertDpToPx(this, 1));
        }

        boolean pickMode = false;
        boolean fullscreen = AppData.fullscreen.get();
        if (getIntent() != null) {
            if (Intent.ACTION_PICK.equals(getIntent().getAction()))
                pickMode = true;

            fullscreen = getIntent().getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, fullscreen);
        }

        if (fullscreen)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, BrowserHistoryFragment.newInstance(pickMode))
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (!(fragment instanceof BrowserHistoryFragment) || !((BrowserHistoryFragment) fragment).onBackPressed()) {
            finish();
        }
    }
}
