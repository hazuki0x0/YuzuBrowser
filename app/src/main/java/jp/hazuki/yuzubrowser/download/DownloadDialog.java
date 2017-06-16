package jp.hazuki.yuzubrowser.download;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.HttpUtils;
import jp.hazuki.yuzubrowser.utils.WebDownloadUtils;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListButton;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListViewController;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class DownloadDialog {
    private final DownloadRequestInfo mInfo;
    private final Context mContext;
    private CustomWebView mWebView;
    private EditText filenameEditText;
    private FileListButton folderButton;
    private CheckBox saveArchiveCheckBox;

    public DownloadDialog(final Context context, DownloadRequestInfo info) {
        mInfo = info;
        mContext = context;
    }

    public void show() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.download_dialog, null);
        view.setVisibility(View.GONE);

        filenameEditText = (EditText) view.findViewById(R.id.filenameEditText);
        setRealItemName(view);

        folderButton = (FileListButton) view.findViewById(R.id.folderButton);
        if (mInfo.getFile() != null) {
            folderButton.setText(mInfo.getFile().getParentFile().getName());
            folderButton.setFilePath(mInfo.getFile().getParentFile());
        } else {
            File file = new File(AppData.download_folder.get());
            folderButton.setText(file.getName());
            folderButton.setFilePath(file);
        }
        folderButton.setShowDirectoryOnly(true);
        folderButton.setOnFileSelectedListener(new FileListViewController.OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file) {
                if (file != null)
                    folderButton.setText(file.getName());
            }

            @Override
            public boolean onDirectorySelected(File file) {
                return false;
            }
        });

        saveArchiveCheckBox = (CheckBox) view.findViewById(R.id.saveArchiveCheckBox);
        setCanSaveArchive(mWebView);

        saveArchiveCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mInfo.setFile(WebDownloadUtils.guessDownloadFile(mInfo.getFile().getParent(), mInfo.getUrl(), null, "application/x-webarchive-xml"));//TODO contentDisposition
                } else {
                    mInfo.setFile(WebDownloadUtils.guessDownloadFile(mInfo.getFile().getParent(), mInfo.getUrl(), null, null));//TODO contentDisposition, mimetype
                }
                filenameEditText.setText(mInfo.getFile().getName());
            }
        });

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.download)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String filename = filenameEditText.getText().toString();
                        if (TextUtils.isEmpty(filename)) {
                            show();
                            return;
                        }

                        File file = new File(folderButton.getCurrentFolder(), filename);
                        mInfo.setFile(file);

                        if (!file.exists()) {
                            startDownload(mInfo);
                        } else {
                            new AlertDialog.Builder(mContext)
                                    .setTitle(R.string.confirm)
                                    .setMessage(R.string.download_file_overwrite)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startDownload(mInfo);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            show();
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            show();
                                        }
                                    })
                                    .show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setRealItemName(final View view) {
        final Handler handler = new Handler();
        if (mInfo.isSolvedFileName()) {
            filenameEditText.setText(mInfo.getFile().getName());
            view.setVisibility(View.VISIBLE);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(mInfo.getUrl());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("HEAD");
                        conn.setConnectTimeout(1000);
                        String cookie = CookieManager.getInstance().getCookie(mInfo.getUrl());
                        if (!TextUtils.isEmpty(cookie)) {
                            conn.setRequestProperty("Cookie", cookie);
                        }
                        String referer = mInfo.getReferer();
                        if (!TextUtils.isEmpty(referer)) {
                            conn.setRequestProperty("Referer", referer);
                        }
                        conn.connect();
                        final File file = HttpUtils.getFileName(mInfo.getUrl(), null, conn.getHeaderFields());
                        mInfo.setFile(file);
                        conn.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mInfo.setFile(WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), mInfo.getUrl(), null, null, mInfo.getDefaultExt()));
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            filenameEditText.setText(mInfo.getFile().getName());
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }).start();
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected DownloadInfo getDownloadInfo() {
        return mInfo;
    }

    protected void startDownload(DownloadInfo info) {
        if (isSaveArchive()) {
            File file = info.getFile();
            mWebView.saveWebArchiveMethod(file.getAbsolutePath());
            Context context = getContext().getApplicationContext();
            if (file.exists())
                Toast.makeText(context, context.getString(R.string.saved_file) + info.getFile().getAbsolutePath(), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show();
        } else {
            DownloadService.startDownloadService(mContext, mInfo);
        }
    }

    public void setCanSaveArchive(CustomWebView webview) {
        mWebView = webview;
        if (saveArchiveCheckBox != null) {
            saveArchiveCheckBox.setVisibility((webview != null) ? View.VISIBLE : View.GONE);
        }
    }

    protected boolean isSaveArchive() {
        return saveArchiveCheckBox.getVisibility() == View.VISIBLE && saveArchiveCheckBox.isChecked();
    }

    public static DownloadDialog showDownloadDialog(Context context, String url, String userAgent, String contentDisposition, String mimetype, long contentLength, String referer) {
        File file = WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, contentDisposition, mimetype);
        DownloadRequestInfo info = new DownloadRequestInfo(url, file, null, contentLength, !TextUtils.isEmpty(contentDisposition));//TODO referer
        return showDownloadDialog(context, info);
    }

    public static DownloadDialog showDownloadDialog(Context context, String url) {
        return showDownloadDialog(context, url, null, null);
    }

    public static DownloadDialog showDownloadDialog(Context context, String url, String referer, String defaultExt) {
        File file = WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, null, null, defaultExt);
        DownloadRequestInfo info = new DownloadRequestInfo(url, file, referer, -1);
        info.setDefaultExt(defaultExt);
        return showDownloadDialog(context, info);
    }

    public static DownloadDialog showDownloadDialog(Context context, DownloadRequestInfo info) {
        DownloadDialog dialog = new DownloadDialog(context, info);
        dialog.show();
        return dialog;
    }

    public static DownloadDialog showArchiveDownloadDialog(Context context, String url, CustomWebView webview) {
        DownloadDialog dialog = showDownloadDialog(context, url);
        dialog.setCanSaveArchive(webview);
        return dialog;
    }
}
