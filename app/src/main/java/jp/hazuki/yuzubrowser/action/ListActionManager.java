package jp.hazuki.yuzubrowser.action;

public abstract class ListActionManager extends ActionManager {
    public abstract void addAction(int id, Action action);

    public abstract ActionList getActionList(int id);
}
