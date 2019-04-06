package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionList;
import jp.hazuki.yuzubrowser.legacy.action.ListActionManager;

public class QuickControlActionManager extends ListActionManager {
    private static final String FOLDER_NAME = "action1_qc";

    private static final int LEVEL1 = 0x011;
    private static final int LEVEL2 = 0x012;
    private static final int LEVEL3 = 0x013;

    public final ActionArrayFile level1 = new ActionArrayFile(FOLDER_NAME, LEVEL1);
    public final ActionArrayFile level2 = new ActionArrayFile(FOLDER_NAME, LEVEL2);
    public final ActionArrayFile level3 = new ActionArrayFile(FOLDER_NAME, LEVEL3);

    @Override
    public void addAction(int id, Action action) {
        getActionList(id).add(action);
    }

    @Override
    public ActionList getActionList(int id) {
        switch (id) {
            case LEVEL1:
                return level1.getList();
            case LEVEL2:
                return level2.getList();
            case LEVEL3:
                return level3.getList();
        }
        throw new IllegalArgumentException("Unknown id:" + id);
    }

    public static QuickControlActionManager getInstance(Context context) {
        QuickControlActionManager manager = new QuickControlActionManager();
        manager.load(context);
        return manager;
    }
}
