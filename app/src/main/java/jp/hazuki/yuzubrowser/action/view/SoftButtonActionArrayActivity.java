package jp.hazuki.yuzubrowser.action.view;

import android.content.Intent;
import android.os.Bundle;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionManager;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class SoftButtonActionArrayActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(Intent.EXTRA_TITLE);
            setTitle(title);

            int actionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0);
            if (actionType == 0) throw new IllegalArgumentException("Unknown action type");
            int actionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0);
            if (actionId == 0) throw new IllegalArgumentException("Unknown action id");

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SoftButtonActionArrayFragment.newInstance(actionType, actionId))
                    .commit();
        }
    }


}
