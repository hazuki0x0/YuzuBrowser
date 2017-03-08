package jp.hazuki.yuzubrowser.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionNameArray;

public class GestureListActivity extends AppCompatActivity {

    private int mGestureId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        if (intent == null)
            throw new IllegalStateException("intent is null");

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null)
            setTitle(title);

        ActionNameArray actionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);
        if (actionNameArray == null)
            actionNameArray = new ActionNameArray(getApplicationContext());

        mGestureId = intent.getIntExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, -1);
        if (mGestureId < 0)
            throw new IllegalStateException("Unknown intent id:" + mGestureId);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, GestureListFragment.newInstance(mGestureId, actionNameArray))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.gesture_test).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), GestureTestActivity.class);
                intent.putExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, mGestureId);
                intent.putExtra(Intent.EXTRA_TITLE, getTitle());
                startActivity(intent);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
