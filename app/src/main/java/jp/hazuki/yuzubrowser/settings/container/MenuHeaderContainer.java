package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.utils.ErrorReport;

public class MenuHeaderContainer implements Containable {
    public final ArrayList<Containable> mPreferenceList = new ArrayList<>();

    public MenuHeaderContainer() {

        checkPreferenceList();
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        for (Containable pref : mPreferenceList) {
            pref.read(shared_preference);
        }
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        for (Containable pref : mPreferenceList) {
            pref.write(editor);
        }
    }

    private void checkPreferenceList() {
        //if (!mPreferenceList.isEmpty())
        //	return;
        try {
            Field[] fields = MenuHeaderContainer.class.getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(this);
                if (obj instanceof Containable) {
                    mPreferenceList.add((Containable) obj);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }
}
