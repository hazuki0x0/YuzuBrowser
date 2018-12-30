package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.action.ActionManager;
import jp.hazuki.yuzubrowser.legacy.action.view.SoftButtonActionArrayActivity;

public class ToolbarActionListPrerence extends Preference {
    private final int mActionId;
    private final int mActionType;
    private final String mTitle;

    public ToolbarActionListPrerence(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToolbarActionListPrerence);
        mActionId = a.getInt(R.styleable.ToolbarActionListPrerence_actionId, 0);
        mActionType = a.getInt(R.styleable.ToolbarActionListPrerence_actionGroup, 0);
        mTitle = a.getString(R.styleable.ToolbarActionListPrerence_android_title);

        if (mActionId == 0)
            throw new IllegalArgumentException("mToolbarId is zero");
        if (mActionType == 0)
            throw new IllegalArgumentException("mActionType is zero");

        a.recycle();
    }

    @Override
    protected void onClick() {
        super.onClick();

        Context context = getContext();
        Intent intent = new Intent(context.getApplicationContext(), SoftButtonActionArrayActivity.class);
        if (mTitle != null) intent.putExtra(Intent.EXTRA_TITLE, mTitle);
        intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, mActionId);
        intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, mActionType);
        context.startActivity(intent);
    }
}
