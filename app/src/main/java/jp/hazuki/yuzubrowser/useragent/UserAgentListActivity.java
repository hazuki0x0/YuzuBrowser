package jp.hazuki.yuzubrowser.useragent;

import android.content.Intent;
import android.os.Bundle;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class UserAgentListActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserAgentListDialog
                .newInstance(getIntent().getStringExtra(Intent.EXTRA_TEXT))
                .show(getSupportFragmentManager(), "ua");
    }

    @Override
    protected int lightThemeResource() {
        return R.style.BrowserMinThemeLight_Transparent;
    }
}
