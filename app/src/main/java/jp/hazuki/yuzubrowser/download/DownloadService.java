package jp.hazuki.yuzubrowser.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.PackageUtils;
import jp.hazuki.yuzubrowser.utils.net.HttpClientBuilder;
import jp.hazuki.yuzubrowser.utils.net.HttpResponseData;

public class DownloadService extends Service {
    public static final String TAG = "DownloadService";
    public static final String EXTRA_DOWNLOAD_INFO = "jp.hazuki.yuzubrowser.download.DownloadService.extra.EXTRA_DOWNLOAD_INFO";
    public static final int NOTIFICATION_INTERVAL = 1000;

    private static final List<Messenger> mObservers = new ArrayList<>();
    private Messenger mMessenger;
    private Handler mHandler;
    private DownloadInfoDatabase mDb;
    private NotificationManager mNotificationManager;
    private PowerManager mPowerManager;
    private static final List<DownloadThread> mThreadList = new LinkedList<>();

    public static final int REGISTER_OBSERVER = 0;
    public static final int UNREGISTER_OBSERVER = 1;
    public static final int UPDATE_PROGRESS = 2;
    public static final int UPDATE_STATE = 3;
    public static final int GET_DOWNLOAD_INFO = 4;
    public static final int CANCEL_DOWNLOAD = 5;

    private static class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SWITCH:
            switch (msg.what) {
                case REGISTER_OBSERVER:
                    mObservers.add(msg.replyTo);
                    break;
                case UNREGISTER_OBSERVER:
                    mObservers.remove(msg.replyTo);
                    break;
                case UPDATE_PROGRESS:
                case UPDATE_STATE: {
                    Iterator<Messenger> it = mObservers.iterator();
                    while (it.hasNext()) {
                        Messenger messenger = it.next();
                        try {
                            messenger.send(Message.obtain(msg));
                        } catch (RemoteException e) {
                            ErrorReport.printAndWriteLog(e);
                            it.remove();
                        }
                    }
                }
                break;
                case GET_DOWNLOAD_INFO: {
                    List<DownloadRequestInfo> list = new ArrayList<>();
                    synchronized (mThreadList) {
                        for (DownloadThread thread : mThreadList) {
                            list.add(thread.getDownloadRequestInfo());
                        }
                    }

                    Iterator<Messenger> it = mObservers.iterator();
                    while (it.hasNext()) {
                        Messenger messenger = it.next();
                        try {
                            messenger.send(Message.obtain(null, GET_DOWNLOAD_INFO, list));
                        } catch (RemoteException e) {
                            ErrorReport.printAndWriteLog(e);
                            it.remove();
                        }
                    }
                }
                break;
                case CANCEL_DOWNLOAD: {
                    long id = (Long) msg.obj;
                    synchronized (mThreadList) {
                        for (DownloadThread thread : mThreadList) {
                            if (id == thread.getDownloadRequestInfo().getId()) {
                                thread.abort();
                                break SWITCH;
                            }
                        }
                        Logger.e(TAG, "id not found:" + id);
                    }
                }
                break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public static Messenger registerObserver(IBinder binder, Messenger activityMessenger) {
        Messenger messenger = new Messenger(binder);
        Message msg = Message.obtain(null, REGISTER_OBSERVER);
        msg.replyTo = activityMessenger;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return messenger;
    }

    public static void unregisterObserver(Messenger serviceMessenger, Messenger activityMessenger) {
        Message msg = Message.obtain(null, UNREGISTER_OBSERVER);
        msg.replyTo = activityMessenger;
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    public static void getDownloadInfo(Messenger serviceMessenger, Messenger activityMessenger) {
        Message msg = Message.obtain(null, GET_DOWNLOAD_INFO);
        msg.replyTo = activityMessenger;
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    public static void cancelDownload(Messenger serviceMessenger, Messenger activityMessenger, long id) {
        Message msg = Message.obtain(null, CANCEL_DOWNLOAD);
        msg.replyTo = activityMessenger;
        msg.obj = id;
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMessenger = new Messenger(new ServiceHandler());
        mHandler = new Handler();
        mDb = new DownloadInfoDatabase(getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        mDb.fixData();
        startForeground(1, getNotify());
    }

    private Notification getNotify() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.download_service));
        builder.setSmallIcon(R.drawable.ic_yuzubrowser_white);
        builder.setPriority(Notification.PRIORITY_MIN);
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (mThreadList) {
            for (DownloadThread thread : mThreadList) {
                thread.forceAbort();
            }
        }
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            DownloadRequestInfo info = (DownloadRequestInfo) intent.getSerializableExtra(EXTRA_DOWNLOAD_INFO);
            if (info != null) {
                new DownloadThread(info).start();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    public static void startDownloadService(Context context, DownloadInfo info) {
        Intent dintent = new Intent(context, DownloadService.class);
        dintent.putExtra(DownloadService.EXTRA_DOWNLOAD_INFO, info);
        context.startService(dintent);
    }

    public class DownloadThread extends Thread {
        private static final int DOWNLOAD_BUFFER_SIZE = 1024 * 10;
        private final DownloadRequestInfo mData;
        private boolean mAbort = false;
        private boolean mForceAbort = false;

        public DownloadThread(DownloadRequestInfo data) {
            mData = data;
        }

        public void forceAbort() {
            mForceAbort = true;
            mAbort = true;
        }

        public void abort() {
            mForceAbort = false;
            mAbort = true;
        }

        public DownloadRequestInfo getDownloadRequestInfo() {
            return mData;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            HttpClientBuilder httpClient = HttpClientBuilder.createInstance(mData.getUrl());
            if (httpClient == null) {
                showToast("HttpClientBuilder is null");
                return;
            }

            OutputStream outputStream = null;
            InputStream inputStream = null;
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
            WakeLock wakelock = null;

            long id = mDb.insert(mData);
            if (id < 0) {
                showToast("DownloadInfoDatabase#insert failed");
                return;
            }

            try {
                synchronized (mThreadList) {
                    mThreadList.add(this);
                }

                wakelock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DownloadThread");
                wakelock.acquire();

                notification.setSmallIcon(android.R.drawable.stat_sys_download);
                notification.setOngoing(true);
                notification.setContentTitle(mData.getFile().getName());
                //notification.setContentText(mData.getUrl());
                notification.setWhen(mData.start_time);
                notification.setProgress(0, 0, true);
                notification.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), DownloadListActivity.class), 0));
                mNotificationManager.notify((int) id, notification.build());//long to int

                String cookie = CookieManager.getInstance().getCookie(mData.getUrl());
                if (!TextUtils.isEmpty(cookie)) {
                    httpClient.setHeader("Cookie", cookie);
                }

                String referer = mData.getReferer();
                if (!TextUtils.isEmpty(referer)) {
                    httpClient.setHeader("Referer", referer);
                }

                HttpResponseData response = httpClient.connect();
                if (response == null) return;

                File file = mData.getFile();
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                outputStream = new FileOutputStream(file);
                inputStream = response.getInputStream();

                int max = (int) response.getContentLength();
                mData.setMaxLength(max);
                int progress = 0;
                int n;
                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                long old_sec = System.currentTimeMillis();
                long new_sec = old_sec;

                while ((n = inputStream.read(buffer)) >= 0) {
                    if (mAbort) break;

                    outputStream.write(buffer, 0, n);
                    progress += n;

                    if (new_sec > old_sec + NOTIFICATION_INTERVAL) {
                        notification.setProgress(max, progress, (max <= 0));
                        notification.setContentText(mData.getNotificationString(getApplicationContext()));
                        mNotificationManager.notify((int) id, notification.build());//long to int
                        mData.setCurrentLength(progress);

                        try {
                            mMessenger.send(Message.obtain(null, UPDATE_PROGRESS, mData));
                        } catch (RemoteException e) {
                            ErrorReport.printAndWriteLog(e);
                        }

                        old_sec = new_sec;
                    }
                    new_sec = System.currentTimeMillis();//busy?
                }

                outputStream.flush();
                outputStream.close();
                outputStream = null;
                inputStream.close();
                inputStream = null;

                if (!mAbort)
                    mData.setState(DownloadInfo.STATE_DOWNLOADED);
                else if (mForceAbort)
                    mData.setState(DownloadInfo.STATE_UNKNOWN_ERROR);
                else
                    mData.setState(DownloadInfo.STATE_CANCELED);

                mDb.updateState(mData);

                try {
                    mMessenger.send(Message.obtain(null, UPDATE_STATE, mData));
                } catch (RemoteException e) {
                    ErrorReport.printAndWriteLog(e);
                }
            } catch (IOException e) {
                ErrorReport.printAndWriteLog(e);
            } finally {
                switch (mData.getState()) {
                    case DownloadInfo.STATE_CANCELED:
                        mNotificationManager.cancel((int) id);
                        break;
                    case DownloadInfo.STATE_DOWNLOADED: {
                        notification.setOngoing(false);
                        notification.setContentTitle(mData.getFile().getName());
                        notification.setWhen(System.currentTimeMillis());
                        notification.setProgress(0, 0, false);
                        notification.setAutoCancel(true);
                        notification.setContentText(getText(R.string.download_success));
                        notification.setSmallIcon(android.R.drawable.stat_sys_download_done);

                        notification.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, PackageUtils.createFileOpenIntent(DownloadService.this, mData.getFile()), 0));
                        mNotificationManager.notify((int) id, notification.build());

                        FileUtils.notifyImageFile(getApplicationContext(), mData.file.getAbsolutePath());
                        break;
                    }
                    case DownloadInfo.STATE_DOWNLOADING:
                        mData.setState(DownloadInfo.STATE_UNKNOWN_ERROR);
                        mDb.insert(mData);
                    /* no break */
                    default:
                        notification.setOngoing(false);
                        notification.setContentTitle(mData.getFile().getName());
                        notification.setWhen(System.currentTimeMillis());
                        notification.setProgress(0, 0, false);
                        notification.setAutoCancel(true);
                        notification.setContentText(getText(R.string.download_fail));
                        notification.setSmallIcon(android.R.drawable.stat_sys_warning);
                        notification.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), DownloadListActivity.class), 0));
                        mNotificationManager.notify((int) id, notification.build());
                        break;
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ErrorReport.printAndWriteLog(e);
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        ErrorReport.printAndWriteLog(e);
                    }
                }
                httpClient.destroy();

                if (wakelock != null) {
                    wakelock.release();
                }

                synchronized (mThreadList) {
                    mThreadList.remove(this);
                    if (mThreadList.isEmpty()) {
                        stopSelf();
                    }
                }
            }
        }

        private void showToast(final String str) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
