package jp.hazuki.yuzubrowser.webencode;

import android.content.Intent;
import android.os.Bundle;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class WebTextEncodeListActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebTextEncodeListDialog
                .newInstance(getIntent().getStringExtra(Intent.EXTRA_TEXT))
                .show(getSupportFragmentManager(), "list");
    }

    @Override
    protected int lightThemeResource() {
        return R.style.BrowserMinThemeLight_Transparent;
    }

}
