package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import jp.hazuki.yuzubrowser.Constants;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.app.LongPressFixActivity;

public class BookmarkActivity extends LongPressFixActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        boolean pickMode = false;
        long itemId = -1;
        boolean fullscreen = AppData.fullscreen.get();
        if (intent != null) {
            pickMode = Intent.ACTION_PICK.equals(intent.getAction());
            itemId = intent.getLongExtra("id", -1);

            fullscreen = intent.getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, fullscreen);
        }



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, BookmarkFragment.newInstance(pickMode, itemId))
                .commit();

        if (fullscreen)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof BookmarkFragment) {
            if (((BookmarkFragment) fragment).onBack()) {
                finish();
            }
        }
    }

    @Override
    public void onBackKeyLongPressed() {
        finish();
    }
}
