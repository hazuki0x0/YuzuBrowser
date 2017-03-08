package jp.hazuki.yuzubrowser.settings.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

import jp.hazuki.yuzubrowser.webencode.WebTextEncodeList;

/**
 * Created by hazuki on 17/01/19.
 */

public class WebTextEncodeListPreference extends ListPreference {

    public WebTextEncodeListPreference(Context context) {
        super(context);
        init(context);
    }

    public WebTextEncodeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected View onCreateDialogView() {
        init(getContext());
        return super.onCreateDialogView();
    }

    private void init(Context context) {
        WebTextEncodeList encodes = new WebTextEncodeList();
        encodes.read(context);

        String[] entries = new String[encodes.size()];

        for (int i = 0; encodes.size() > i; i++) {
            entries[i] = encodes.get(i).encoding;
        }

        setEntries(entries);
        setEntryValues(entries);
    }
}
