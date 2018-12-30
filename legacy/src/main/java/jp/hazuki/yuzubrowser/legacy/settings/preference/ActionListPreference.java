package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.action.ActionManager;
import jp.hazuki.yuzubrowser.legacy.action.view.ActionListManagerActivity;

public class ActionListPreference extends Preference {
    private final int mActionId;
    private final int mActionType;
    private final String mTitle;

    public ActionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionListPreference);
        mActionType = a.getInt(R.styleable.ActionListPreference_actionGroup, 0);
        mActionId = a.getInt(R.styleable.ActionListPreference_actionId, 0);
        mTitle = a.getString(R.styleable.ActionListPreference_android_title);

        if (mActionType == 0)
            throw new IllegalArgumentException("mActionType is zero");

        if (mActionId == 0)
            throw new IllegalArgumentException("actionId is zero");

        a.recycle();
    }

    @Override
    protected void onClick() {
        super.onClick();

        Context context = getContext();
        Intent intent = new Intent(context.getApplicationContext(), ActionListManagerActivity.class);
        if (mTitle != null) intent.putExtra(Intent.EXTRA_TITLE, mTitle);
        intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, mActionType);
        intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, mActionId);
        context.startActivity(intent);
    }
}
