package jp.hazuki.yuzubrowser.action.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionManager;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile;

public class SoftButtonActionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        int mActionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0);
        int mActionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0);
        setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));

        if (mActionType == 0)
            throw new IllegalArgumentException("actiontype is 0");

        ActionFragment fragment = new ActionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ActionManager.INTENT_EXTRA_ACTION_ID, mActionId);
        bundle.putInt(ActionManager.INTENT_EXTRA_ACTION_TYPE, mActionType);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }


    public static class ActionFragment extends ListFragment {
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            adapter.add(getString(R.string.pref_btn_action_press));
            adapter.add(getString(R.string.pref_btn_action_lpress));
            adapter.add(getString(R.string.pref_btn_action_up));
            adapter.add(getString(R.string.pref_btn_action_down));
            adapter.add(getString(R.string.pref_btn_action_left));
            adapter.add(getString(R.string.pref_btn_action_right));
            setListAdapter(adapter);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            int actionId = getArguments().getInt(ActionManager.INTENT_EXTRA_ACTION_ID);
            switch (position) {
                case 0:
                    actionId |= SoftButtonActionFile.BUTTON_SWIPE_PRESS;
                    break;
                case 1:
                    actionId |= SoftButtonActionFile.BUTTON_SWIPE_LPRESS;
                    break;
                case 2:
                    actionId |= SoftButtonActionFile.BUTTON_SWIPE_UP;
                    break;
                case 3:
                    actionId |= SoftButtonActionFile.BUTTON_SWIPE_DOWN;
                    break;
                case 4:
                    actionId |= SoftButtonActionFile.BUTTON_SWIPE_LEFT;
                    break;
                case 5:
                    actionId |= SoftButtonActionFile.BUTTON_SWIPE_RIGHT;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown position:" + position);
            }

            new ActionActivity.Builder(getActivity())
                    .setTitle(getActivity().getTitle())
                    .setActionManager(getArguments().getInt(ActionManager.INTENT_EXTRA_ACTION_TYPE), actionId)
                    .show();
        }
    }

    public static class Builder {
        private final Context mContext;
        private final Intent mIntent;

        public Builder(Context context) {
            mContext = context;
            mIntent = new Intent(context.getApplicationContext(), SoftButtonActionActivity.class);
        }

        public Builder setTitle(int title) {
            mIntent.putExtra(Intent.EXTRA_TITLE, mContext.getString(title));
            return this;
        }

        public Builder setTitle(CharSequence title) {
            mIntent.putExtra(Intent.EXTRA_TITLE, title);
            return this;
        }

        public Builder setActionManager(int actionType, int actionId) {
            mIntent.putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, actionType);
            mIntent.putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, actionId);
            return this;
        }

        public void show() {
            mContext.startActivity(mIntent);
        }

        public Intent create() {
            return mIntent;
        }

        public void show(int requestCode) {
            if (mContext instanceof Activity)
                ((Activity) mContext).startActivityForResult(mIntent, requestCode);
            else
                throw new IllegalArgumentException("Context is not instanceof Activity");
        }
    }
}
