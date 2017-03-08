package jp.hazuki.yuzubrowser.utils.service;

import android.os.IBinder;

public interface ServiceConnectionHelper<T> {
    T onBind(IBinder service);

    void onUnbind(T service);
}
