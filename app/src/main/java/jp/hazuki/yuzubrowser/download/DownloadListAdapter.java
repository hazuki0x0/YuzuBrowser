package jp.hazuki.yuzubrowser.download;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.Logger;

public class DownloadListAdapter extends ResourceCursorAdapter {
    private static final String TAG = "DownloadListAdapter";
    private final Context mContext;

    public DownloadListAdapter(Context context, Cursor cursor) {
        super(context, R.layout.download_list_item, cursor, 0);
        mContext = context;
    }

    private LongSparseArray<DownloadRequestInfo> mDownloadingList = new LongSparseArray<>();

    public void pushDownloadList(DownloadRequestInfo info) {
        mDownloadingList.put(info.getId(), info);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.urlTextView)).setText(cursor.getString(DownloadInfoDatabase.COLUMN_URL_INDEX));
        ((TextView) view.findViewById(R.id.filenameTextView)).setText(new File(cursor.getString(DownloadInfoDatabase.COLUMN_FILEPATH_INDEX)).getName());

        TextView statusTextView = (TextView) view.findViewById(R.id.statusTextView);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        switch (cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX)) {
            case DownloadInfo.STATE_DOWNLOADED:
                statusTextView.setText(mContext.getText(R.string.download_success));
                progressBar.setVisibility(View.GONE);
                break;
            case DownloadInfo.STATE_CANCELED:
                statusTextView.setText(mContext.getText(R.string.download_cancel));
                progressBar.setVisibility(View.GONE);
                break;
            case DownloadInfo.STATE_DOWNLOADING: {
                long id = cursor.getLong(DownloadInfoDatabase.COLUMN_ID_INDEX);
                DownloadRequestInfo info = mDownloadingList.get(id);
                if (info != null) {
                    statusTextView.setText(info.getNotificationString(context));

                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(info.getCurrentLength());
                    progressBar.setMax(info.getMaxLength());
                    progressBar.setIndeterminate(info.getMaxLength() <= 0);
                    //mDownloadingList.remove(info);
                } else {
                    Logger.i(TAG, "id:" + id + " DownloadRequestInfo not found");
                }
            }
            break;
            default:
                statusTextView.setText(mContext.getText(R.string.download_fail));
                progressBar.setVisibility(View.GONE);
                break;
        }
    }
}
