package jp.hazuki.yuzubrowser.action.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import jp.hazuki.yuzubrowser.Constants;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionManager;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.ListActionManager;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.SingleActionManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.app.OnActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class ActionActivity extends ThemeActivity {
    private static final String TAG = "ActionActivity";
    public static final String ACTION_ALL_ACTION = "ActionActivity.action.allaction";
    public static final String EXTRA_ACTION = "ActionActivity.extra.action";
    public static final String EXTRA_RETURN = "ActionActivity.extra.return";
    public static final int RESULT_REQUEST_PREFERENCE = 1;

    private ActionManager mActionManager;
    private OnActivityResultListener mOnActivityResultListener;
    private ActionNameArray mActionNameArray;
    private int mActionId;
    private Action mAction = null;
    private ListView listView;
    private ActionNameArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.action_activity);
        listView = (ListView) findViewById(R.id.listView);
        Button okButton = (Button) findViewById(R.id.okButton);
        Button resetButton = (Button) findViewById(R.id.resetButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        if (intent == null) throw new NullPointerException("intent is null");

        if (ACTION_ALL_ACTION.equals(intent.getAction())) {
            boolean fullscreen = intent.getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, AppData.fullscreen.get());
            int orientation = intent.getIntExtra(Constants.intent.EXTRA_MODE_ORIENTATION, AppData.oritentation.get());

            if (fullscreen)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(orientation);
        }

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);
        ActionNameArray actionNameArray = (mActionNameArray == null) ? new ActionNameArray(getApplicationContext()) : mActionNameArray;

        adapter = new ActionNameArrayAdapter(this, actionNameArray);
        listView.setAdapter(adapter);

        int mActionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0);
        mActionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0);
        if (mActionType != 0) {
            mActionManager = ActionManager.getActionManager(getApplicationContext(), mActionType);

            if (mActionManager instanceof SingleActionManager)
                mAction = new Action(((SingleActionManager) mActionManager).getAction(mActionId));//copy
            else if (mActionManager instanceof ListActionManager)
                mAction = new Action();
            else
                throw new IllegalArgumentException();
        } else {
            mActionManager = null;
            mAction = intent.getParcelableExtra(EXTRA_ACTION);
            if (mAction == null)
                mAction = new Action();
        }

        setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));

        int initial_position = -1;
        for (SingleAction action : mAction) {
            int id = action.id;
            int size = actionNameArray.actionValues.length;
            for (int i = 0; i < size; ++i) {
                if (actionNameArray.actionValues[i] == id) {
                    adapter.setChecked(i, true);

                    if (initial_position == -1)
                        initial_position = i;
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (initial_position != -1)
            listView.setSelection(initial_position);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int value = adapter.getItemValue(position);

                if (adapter.toggleCheck(position)) {
                    SingleAction action = SingleAction.makeInstance(value);
                    mAction.add(action);
                    showPreference(action.showMainPreference(ActionActivity.this));
                } else {
                    for (int i = 0; i < mAction.size(); i++) {
                        if (mAction.get(i).id == value) {
                            mAction.remove(i);
                            break;
                        }
                    }
                }

            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!adapter.isChecked(position))
                    listView.performItemClick(view, position, id);

                showSubPreference(position);
                return true;
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mActionManager == null) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_ACTION, (Parcelable) mAction);
                    intent.putExtra(EXTRA_RETURN, getIntent().getBundleExtra(EXTRA_RETURN));
                    setResult(RESULT_OK, intent);
                } else if (mActionManager instanceof SingleActionManager) {
                    Action list = ((SingleActionManager) mActionManager).getAction(mActionId);
                    list.clear();
                    list.addAll(mAction);
                    mActionManager.save(getApplicationContext());
                    setResult(RESULT_OK);
                } else if (mActionManager instanceof ListActionManager) {
                    ((ListActionManager) mActionManager).addAction(mActionId, mAction);
                    mActionManager.save(getApplicationContext());
                    setResult(RESULT_OK);
                }
                finish();
            }
        });

        okButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startJsonStringActivity();
                return false;
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAction.clear();
                adapter.clearChoices();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        adapter.setListener(new ActionNameArrayAdapter.OnSettingButtonListener() {
            @Override
            public void onSettingClick(int position) {
                showSubPreference(position);
            }
        });
    }

    public ActionNameArray getActionNameArray() {
        return mActionNameArray;
    }

    private void showSubPreference(int position) {
        int value = adapter.getItemValue(position);
        int size = mAction.size();
        for (int i = 0; i < size; i++) {
            if (mAction.get(i).id == value) {
                showPreference(mAction.get(i).showSubPreference(ActionActivity.this));
                break;
            }
        }
    }

    private boolean showPreference(StartActivityInfo screen) {
        if (screen == null)
            return false;
        mOnActivityResultListener = screen.getOnActivityResultListener();
        startActivityForResult(screen.getIntent(), RESULT_REQUEST_PREFERENCE);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REQUEST_PREFERENCE:
                if (mOnActivityResultListener != null) {
                    mOnActivityResultListener.onActivityResult(this, resultCode, data);
                    mOnActivityResultListener = null;
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.action_to_json).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startJsonStringActivity();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void startJsonStringActivity() {
        Intent intent = new Intent(getApplicationContext(), ActionStringActivity.class);
        intent.putExtra(ActionStringActivity.EXTRA_ACTION, (Parcelable) mAction);
        startActivity(intent);
    }

    public static Action getActionFromIntent(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            Logger.w(TAG, "resultCode != Activity.RESULT_OK || intent == null");
            return null;
        }
        return intent.getParcelableExtra(EXTRA_ACTION);
    }

    public static Action getActionFromIntent(Intent intent) {
        return intent.getParcelableExtra(EXTRA_ACTION);
    }

    public static Bundle getReturnData(Intent intent) {
        return intent.getBundleExtra(EXTRA_RETURN);
    }

    public interface OnActionActivityResultListener {
        void onActionActivityResult(Context context, Action action);
    }

    public static class Builder {
        private final Context mContext;
        private final Intent mIntent;
        private OnActivityResultListener mListener;

        public Builder(Context context) {
            mContext = context;
            mIntent = new Intent(context.getApplicationContext(), ActionActivity.class);
        }

        public Builder setTitle(int title) {
            mIntent.putExtra(Intent.EXTRA_TITLE, mContext.getString(title));
            return this;
        }

        public Builder setTitle(CharSequence title) {
            mIntent.putExtra(Intent.EXTRA_TITLE, title);
            return this;
        }

        public Builder setActionNameArray(ActionNameArray array) {
            mIntent.putExtra(ActionNameArray.INTENT_EXTRA, (Parcelable) array);
            return this;
        }

        public Builder setActionManager(int actionType, int actionId) {
            mIntent.putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, actionType);
            mIntent.putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, actionId);
            return this;
        }

        public Builder setDefaultAction(Action action) {
            mIntent.putExtra(EXTRA_ACTION, (Parcelable) action);
            return this;
        }

        public Builder setReturnData(Bundle bundle) {
            mIntent.putExtra(EXTRA_RETURN, bundle);
            return this;
        }

        public Builder setOnActionActivityResultListener(final OnActionActivityResultListener l) {
            mListener = new OnActivityResultListener() {
                private static final String TAG = "ActionActivityResult";

                @Override
                public void onActivityResult(Context context, int resultCode, Intent intent) {
                    Action action = getActionFromIntent(resultCode, intent);
                    if (action == null) {
                        Logger.w(TAG, "Action is null");
                        return;
                    }
                    l.onActionActivityResult(context, action);
                }
            };
            return this;
        }

        public StartActivityInfo makeStartActivityInfo() {
            return new StartActivityInfo(mIntent, mListener);
        }

        public Intent create() {
            return mIntent;
        }

        public void show() {
            mContext.startActivity(mIntent);
        }

        public OnActivityResultListener show(int requestCode) {
            if (mContext instanceof Activity)
                ((Activity) mContext).startActivityForResult(mIntent, requestCode);
            else
                throw new IllegalArgumentException("Context is not instanceof Activity");

            return mListener;
        }
    }
}
