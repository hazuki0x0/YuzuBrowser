package jp.hazuki.yuzubrowser.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.useragent.UserAgentSettingActivity;

/**
 * Created by hazuki on 17/01/19.
 */

public class UserAgentListSettingsPreference extends Preference {
    public UserAgentListSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        super.onClick();
        Intent intent = new Intent(getContext(), UserAgentSettingActivity.class);
        getContext().startActivity(intent);
    }
}
