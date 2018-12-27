package jp.hazuki.yuzubrowser.legacy.settings.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.legacy.settings.container.Containable;
import jp.hazuki.yuzubrowser.legacy.utils.ErrorReport;

public class LocalData {
    private final String mPreferenceName;

    protected LocalData(String preferenceName) {
        mPreferenceName = preferenceName;
    }

    public boolean load(Context context) {
        SharedPreferences shared_preference = context.getSharedPreferences(mPreferenceName, Context.MODE_PRIVATE);
        for (Containable pref : getPreferenceList()) {
            pref.read(shared_preference);
        }
        return true;
    }

    public boolean commit(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(mPreferenceName, Context.MODE_PRIVATE).edit();
        for (Containable pref : getPreferenceList()) {
            pref.write(editor);
        }
        return editor.commit();
    }

    protected ArrayList<Containable> getPreferenceList() {
        ArrayList<Containable> list;

        list = new ArrayList<>();
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(this);
                if (obj instanceof Containable) {// null returns false
                    list.add((Containable) obj);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }

        return list;
    }
}
