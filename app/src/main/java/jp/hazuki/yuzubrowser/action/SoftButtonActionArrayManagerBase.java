package jp.hazuki.yuzubrowser.action;

import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionArrayFile;

public abstract class SoftButtonActionArrayManagerBase extends SingleActionManager {
    public abstract SoftButtonActionArrayFile getActionArrayFile(int id);

    public abstract int makeActionIdFromPosition(int id, int position);

    public int getMax() {
        return 0x7F;
    }
}
