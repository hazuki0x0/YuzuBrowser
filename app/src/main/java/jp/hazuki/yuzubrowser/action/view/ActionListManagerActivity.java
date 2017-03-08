package jp.hazuki.yuzubrowser.action.view;

import android.content.Intent;
import android.os.Bundle;

import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionManager;
import jp.hazuki.yuzubrowser.action.ListActionManager;

public class ActionListManagerActivity extends ActionListActivity {
    private ListActionManager mActionManager;
    private ActionList actions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent == null) throw new NullPointerException("intent is null");
        int mActionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0);
        if (mActionType == 0) throw new IllegalArgumentException("Unknown action type");
        int mActionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0);
        if (mActionId == 0) throw new IllegalArgumentException("Unknown action id");

        ActionManager action_manager = ActionManager.getActionManager(this, mActionType);

        if (!(action_manager instanceof ListActionManager))
            throw new IllegalArgumentException();

        mActionManager = (ListActionManager) action_manager;
        actions = mActionManager.getActionList(mActionId);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected ActionList getActionList() {
        return actions;
    }

    @Override
    public void onActionListChanged(ActionList actionList) {
        mActionManager.save(getApplicationContext());
    }
}
