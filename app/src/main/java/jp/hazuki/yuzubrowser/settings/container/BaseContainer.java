package jp.hazuki.yuzubrowser.settings.container;

import android.content.SharedPreferences;

public abstract class BaseContainer<T> implements Containable {
    protected BaseContainer(String name, T def_value) {
        mName = name;
        mDefValue = def_value;
        mValue = def_value;
    }

    @Override
    public abstract void read(SharedPreferences shared_preference);

    @Override
    public abstract void write(SharedPreferences.Editor editor);

    public T get() {
        return mValue;
    }

    public void set(T value) {
        mValue = value;
    }

    public void resetToDefaultValue() {
        mValue = mDefValue;
    }

    protected final String mName;
    protected final T mDefValue;
    protected T mValue;
}
