package jp.hazuki.yuzubrowser.download;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;

import jp.hazuki.yuzubrowser.utils.Logger;

public class DownloadListActivityHandler extends Handler {
    private static final String TAG = "DownloadListActivityHandler";
    private final WeakReference<DownloadListActivity> mReference;

    public DownloadListActivityHandler(DownloadListActivity activity) {
        mReference = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        DownloadListActivity activity = mReference.get();
        if (activity == null)
            return;

        switch (msg.what) {
            case DownloadService.UPDATE_PROGRESS:
                if (msg.obj instanceof DownloadRequestInfo)
                    activity.updateProgress((DownloadRequestInfo) msg.obj);
                else
                    Logger.e(TAG, "UPDATE_PROGRESS : msg.obj is not instanceof DownloadRequestInfo");
                break;
            case DownloadService.UPDATE_STATE:
                activity.updateState();
                break;
            case DownloadService.GET_DOWNLOAD_INFO:
                if (msg.obj instanceof List<?>) {
                    List<?> list = (List<?>) msg.obj;
                    for (Object item : list) {
                        if (item instanceof DownloadRequestInfo)
                            activity.pushDownloadList((DownloadRequestInfo) item);
                        else
                            Logger.e(TAG, "UPDATE_PROGRESS : item is not instanceof DownloadRequestInfo");
                    }
                    activity.notifyDataSetChanged();
                } else
                    Logger.e(TAG, "UPDATE_PROGRESS : msg.obj is not instanceof List<?>");
            default:
                super.handleMessage(msg);
                break;
        }
    }
}
