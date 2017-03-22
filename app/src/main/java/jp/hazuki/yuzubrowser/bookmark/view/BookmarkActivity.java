package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.Api24LongPressFix;

public class BookmarkActivity extends AppCompatActivity implements Api24LongPressFix.OnBackLongClickListener {

    private Api24LongPressFix api24LongPressFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        api24LongPressFix = new Api24LongPressFix(this);

        Intent intent = getIntent();
        boolean pickMode = intent != null && Intent.ACTION_PICK.equals(intent.getAction());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, BookmarkFragment.newInstance(pickMode))
                .commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            event.startTracking();
            api24LongPressFix.onBackKeyDown();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (api24LongPressFix.onBackKeyUp()) {
                return true;
            }
            if (event.isTracking() && !event.isCanceled()) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment instanceof BookmarkFragment) {
                    if (((BookmarkFragment) fragment).onBack()) {
                        finish();
                    }
                    return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackLongClick() {
        finish();
    }
}
