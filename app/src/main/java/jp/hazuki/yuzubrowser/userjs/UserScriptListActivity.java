package jp.hazuki.yuzubrowser.userjs;

import android.os.Bundle;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class UserScriptListActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new UserScriptListFragment())
                .commit();
    }
}
