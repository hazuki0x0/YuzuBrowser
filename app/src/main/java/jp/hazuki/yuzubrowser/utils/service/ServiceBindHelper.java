package jp.hazuki.yuzubrowser.utils.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceBindHelper<T> {
    private final Context mContext;
    private boolean mIsBound = false;
    private final ServiceConnectionHelper<T> mConnectionHelper;
    private final ServiceConnection mConnection;
    private T mService;

    public ServiceBindHelper(Context context, ServiceConnectionHelper<T> connection) {
        mContext = context;
        mConnectionHelper = connection;
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = mConnectionHelper.onBind(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
    }

    public void bindService(Intent intent) {
        if (!mIsBound) {
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    public void unbindService() {
        if (mIsBound) {
            mConnectionHelper.onUnbind(mService);
            mContext.unbindService(mConnection);
            mIsBound = false;
            mService = null;
        }
    }

    public T getBinder() {
        return mService;
    }
}
