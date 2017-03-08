package jp.hazuki.yuzubrowser.action.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.app.OnActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class ActionListActivity extends AppCompatActivity {
    public static final String EXTRA_ACTION_LIST = "ActionListActivity.extra.actionList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        ActionList mList = getActionList();

        ActionNameArray mActionNameArray = null;

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(Intent.EXTRA_TITLE);
            setTitle(title);

            mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);
            if (mList == null)
                mList = intent.getParcelableExtra(EXTRA_ACTION_LIST);
        }

        if (mList == null)
            mList = new ActionList();

        if (mActionNameArray == null)
            mActionNameArray = new ActionNameArray(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ActionListFragment.newInstance(mList, mActionNameArray))
                .commit();
    }

    protected ActionList getActionList() {
        return null;
    }

    public void onActionListChanged(ActionList actionList) {
    }


    public interface OnActionListActivityResultListener {
        void onActionListActivityResult(Context context, ActionList actionlist);
    }

    public static class Builder {
        private final Context mContext;
        private final Intent mIntent;
        private OnActivityResultListener mListener;

        public Builder(Context context) {
            mContext = context;
            mIntent = new Intent(context.getApplicationContext(), ActionListActivity.class);
        }

        public Builder setTitle(int title) {
            mIntent.putExtra(Intent.EXTRA_TITLE, mContext.getString(title));
            return this;
        }

        public Builder setTitle(String title) {
            mIntent.putExtra(Intent.EXTRA_TITLE, title);
            return this;
        }

        public Builder setActionNameArray(ActionNameArray array) {
            mIntent.putExtra(ActionNameArray.INTENT_EXTRA, (Parcelable) array);
            return this;
        }

        public Builder setDefaultActionList(ActionList actionlist) {
            mIntent.putExtra(EXTRA_ACTION_LIST, (Parcelable) actionlist);
            return this;
        }

        public Builder setOnActionListActivityResultListener(final OnActionListActivityResultListener l) {
            mListener = new OnActivityResultListener() {
                private static final String TAG = "OnActionActivityResultListener";

                @Override
                public void onActivityResult(Context context, int resultCode, Intent intent) {
                    if (resultCode != Activity.RESULT_OK || intent == null) {
                        Logger.w(TAG, "resultCode != Activity.RESULT_OK || intent == null");
                        return;
                    }
                    ActionList actionlist = intent.getParcelableExtra(EXTRA_ACTION_LIST);
                    if (actionlist == null) {
                        Logger.w(TAG, "Action is null");
                        return;
                    }
                    l.onActionListActivityResult(context, actionlist);
                }
            };
            return this;
        }

        public StartActivityInfo makeStartActivityInfo() {
            return new StartActivityInfo(mIntent, mListener);
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
