package jp.hazuki.yuzubrowser.action.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.utils.util.JsonConvertable;

public class ActionStringActivity extends AppCompatActivity {
    public static final String EXTRA_ACTIVITY = "MakeActionStringActivity.extra.activity";
    public static final String EXTRA_ACTION = "MakeActionStringActivity.extra.action";
    public static final int ACTION_ACTIVITY = 1;
    public static final int ACTION_LIST_ACTIVITY = 2;
    private int mTarget;
    private EditText editText;
    private ActionNameArray mActionNameArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_edittext);
        editText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        if (intent == null)
            throw new NullPointerException("Intent is null");

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);

        Parcelable action = intent.getParcelableExtra(EXTRA_ACTION);
        if (action != null) {
            if (action instanceof Action) {
                mTarget = ACTION_ACTIVITY;
                editText.setText(((JsonConvertable) action).toJsonString());
                return;
            } else if (action instanceof ActionList) {
                mTarget = ACTION_LIST_ACTIVITY;
                editText.setText(((JsonConvertable) action).toJsonString());
                return;
            }
            throw new IllegalArgumentException("EXTRA_ACTION is not action or actionlist");
        } else {
            mTarget = getIntent().getIntExtra(EXTRA_ACTIVITY, ACTION_ACTIVITY);

            switch (mTarget) {
                case ACTION_ACTIVITY:
                    new ActionActivity.Builder(this)
                            .show(ACTION_ACTIVITY);
                    break;
                case ACTION_LIST_ACTIVITY:
                    new ActionListActivity.Builder(this)
                            .show(ACTION_LIST_ACTIVITY);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("to action").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String jsonstr = editText.getText().toString();

                switch (mTarget) {
                    case ACTION_ACTIVITY:
                        new ActionActivity.Builder(ActionStringActivity.this)
                                .setDefaultAction(new Action(jsonstr))
                                .setActionNameArray(mActionNameArray)
                                .show(ACTION_ACTIVITY);
                        break;
                    case ACTION_LIST_ACTIVITY:
                        new ActionListActivity.Builder(ActionStringActivity.this)
                                .setDefaultActionList(new ActionList(jsonstr))
                                .setActionNameArray(mActionNameArray)
                                .show(ACTION_LIST_ACTIVITY);
                        break;
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_ACTIVITY: {
                if (resultCode != Activity.RESULT_OK || data == null)
                    return;
                Action action = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                if (action == null)
                    return;
                editText.setText(action.toJsonString());
            }
            case ACTION_LIST_ACTIVITY: {
                if (resultCode != Activity.RESULT_OK || data == null)
                    return;
                ActionList action = data.getParcelableExtra(ActionListActivity.EXTRA_ACTION_LIST);
                if (action == null)
                    return;
                editText.setText(action.toJsonString());
            }
            break;
        }
    }
}
