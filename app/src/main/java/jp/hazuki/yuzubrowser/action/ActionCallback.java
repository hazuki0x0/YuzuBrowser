package jp.hazuki.yuzubrowser.action;

import android.graphics.drawable.Drawable;
import android.view.View;

public abstract class ActionCallback {
    public abstract Drawable getIcon(SingleAction action);

    public final Drawable getIcon(Action action_list) {
        if (action_list.isEmpty()) return null;
        return getIcon(action_list.get(0));
    }

    public abstract boolean run(SingleAction action, TargetInfo target, View view);

    public final boolean run(SingleAction action) {
        return run(action, null, null);
    }

    public final boolean run(Action list) {
        return run(list, null, null);
    }

    public final boolean run(Action list, View view) {
        return run(list, null, view);
    }

    public final boolean run(Action list, TargetInfo target) {
        return run(list, target, null);
    }

    public final boolean run(Action list, TargetInfo target, View view) {
        if (list.isEmpty()) return false;
        for (SingleAction action : list) {
            run(action, target, view);
        }
        return true;
    }

    public static class TargetInfo {
        private int target;

        public TargetInfo() {
            this.target = -1;
        }

        public TargetInfo(int target) {
            this.target = target;
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int target) {
            this.target = target;
        }
    }
}
