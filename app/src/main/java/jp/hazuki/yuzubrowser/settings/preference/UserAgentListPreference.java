package jp.hazuki.yuzubrowser.settings.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.useragent.UserAgent;
import jp.hazuki.yuzubrowser.useragent.UserAgentList;

/**
 * Created by hazuki on 17/01/19.
 */

public class UserAgentListPreference extends ListPreference {

    public UserAgentListPreference(Context context) {
        super(context);
        init(context);
    }

    public UserAgentListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected View onCreateDialogView() {
        init(getContext());
        return super.onCreateDialogView();
    }

    private void init(Context context) {
        UserAgentList mUserAgentList = new UserAgentList();
        mUserAgentList.read(context);

        String[] entries = new String[mUserAgentList.size() + 1];
        String[] entryValues = new String[mUserAgentList.size() + 1];

        entries[0] = getContext().getString(R.string.default_text);
        entryValues[0] = "";

        UserAgent userAgent;

        for (int i = 1; mUserAgentList.size() > i - 1; i++) {
            userAgent = mUserAgentList.get(i - 1);
            entries[i] = userAgent.name;
            entryValues[i] = userAgent.useragent;
        }

        setEntries(entries);
        setEntryValues(entryValues);
    }
}
