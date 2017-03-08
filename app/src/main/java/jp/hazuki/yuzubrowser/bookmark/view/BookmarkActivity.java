package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import jp.hazuki.yuzubrowser.R;

public class BookmarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        boolean pickMode = intent != null && Intent.ACTION_PICK.equals(intent.getAction());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, BookmarkFragment.newInstance(pickMode))
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
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
}
