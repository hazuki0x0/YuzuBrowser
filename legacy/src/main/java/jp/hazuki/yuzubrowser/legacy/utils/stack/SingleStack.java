package jp.hazuki.yuzubrowser.legacy.utils.stack;

public abstract class SingleStack<T> {
    private boolean pause;

    private T stack = null;

    public void onResume() {
        pause = false;
        if (stack != null) {
            processItem(stack);
            stack = null;
        }
    }

    public void onPause() {
        pause = true;
    }

    public void addItem(T item) {
        if (pause) {
            stack = item;
        } else {
            processItem(item);
        }
    }

    protected abstract void processItem(T item);
}
