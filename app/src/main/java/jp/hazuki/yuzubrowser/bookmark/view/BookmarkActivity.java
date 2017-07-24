package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.app.LongPressFixActivity;

public class BookmarkActivity extends LongPressFixActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        boolean pickMode = false;
        long itemId = -1;
        if (intent != null) {
            pickMode = Intent.ACTION_PICK.equals(intent.getAction());
            itemId = intent.getLongExtra("id", -1);
        }



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, BookmarkFragment.newInstance(pickMode, itemId))
                .commit();
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
