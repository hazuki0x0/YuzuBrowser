package jp.hazuki.yuzubrowser.utils.matcher;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractPatternChecker<T extends AbstractPatternAction> implements Serializable {
    private final T mPatternAction;

    protected AbstractPatternChecker(T pattern_action) {
        mPatternAction = pattern_action;
    }

    public final T getAction() {
        return mPatternAction;
    }

    public abstract String getTitle(Context context);

    public abstract boolean isEnable();

    public abstract void setEnable(boolean enable);

    public String getActionTitle(Context context) {
        return mPatternAction.getTitle(context);
    }

    public abstract boolean write(JsonGenerator generator) throws IOException;
}
