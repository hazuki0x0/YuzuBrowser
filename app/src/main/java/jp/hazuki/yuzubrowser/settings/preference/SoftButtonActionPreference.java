package jp.hazuki.yuzubrowser.settings.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.view.SoftButtonActionActivity;

public class SoftButtonActionPreference extends Preference {
    private final int mActionId;
    private final int mActionType;
    private final String mTitle;

    public SoftButtonActionPreference(Context context, AttributeSet attrs) {
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

        new SoftButtonActionActivity.Builder(getContext())
                .setTitle(mTitle)
                .setActionManager(mActionType, mActionId)
                .show();
    }
}
