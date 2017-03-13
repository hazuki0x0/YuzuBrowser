package jp.hazuki.yuzubrowser.settings.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.backup.BackupTask;
import jp.hazuki.yuzubrowser.backup.RestoreTask;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.common.AlertDialogPreference;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.PermissionUtils;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragment;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListDialog;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListViewController;

/**
 * Created by hazuki on 17/01/25.
 */

public class ImportExportFragment extends PreferenceFragment implements LoaderManager.LoaderCallbacks<Boolean> {

    private static final String EXT = ".yuzubackup";
    private DialogFragment progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AppData.PREFERENCE_NAME);
        addPreferencesFromResource(R.xml.pref_import_export);

        findPreference("import_sd_bookmark").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BookmarkManager manager = new BookmarkManager(getActivity());
                final File internal_file = manager.getBookmarkFile();

                File def_folder = new File(BrowserApplication.getExternalUserDirectory(), internal_file.getParentFile().getName() + File.separator);
                if (!def_folder.exists())
                    def_folder = Environment.getExternalStorageDirectory();

                new FileListDialog(getActivity())
                        .setFilePath(def_folder)
                        .setOnFileSelectedListener(new FileListViewController.OnFileSelectedListener() {
                            @Override
                            public void onFileSelected(final File file) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.pref_import_bookmark)
                                        .setMessage(R.string.pref_import_bookmark_confirm)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (file.exists())
                                                    if (FileUtils.copySingleFile(file, internal_file)) {
                                                        Toast.makeText(getActivity(), R.string.succeed, Toast.LENGTH_LONG).show();
                                                        return;
                                                    }
                                                Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                            }

                            @Override
                            public boolean onDirectorySelected(File file) {
                                return false;
                            }
                        })
                        .show();

                return false;
            }
        });

        ((AlertDialogPreference) findPreference("export_sd_bookmark")).setOnPositiveButtonListener(new AlertDialogPreference.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if (PermissionUtils.checkWriteStorage(getActivity())) {
                    BookmarkManager manager = new BookmarkManager(getActivity());
                    File internal_file = manager.getBookmarkFile();
                    File external_file = new File(BrowserApplication.getExternalUserDirectory(), internal_file.getParentFile().getName() + File.separator + FileUtils.getTimeFileName() + ".dat");
                    if (!external_file.getParentFile().exists()) {
                        if (!external_file.getParentFile().mkdirs()) {
                            Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    if (internal_file.exists())
                        if (FileUtils.copySingleFile(internal_file, external_file)) {
                            Toast.makeText(getActivity(), R.string.succeed, Toast.LENGTH_LONG).show();
                            return;
                        }
                    Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_LONG).show();
                } else {
                    PermissionUtils.requestStorage(getActivity());
                }
            }
        });

        findPreference("restore_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                File dir = new File(BrowserApplication.getExternalUserDirectory(), "backup");
                if (!dir.exists())
                    dir.mkdirs();
                new FileListDialog(getActivity())
                        .setFilePath(dir)
                        .setShowExtensionOnly(EXT)
                        .setOnFileSelectedListener(new FileListViewController.OnFileSelectedListener() {
                            @Override
                            public void onFileSelected(final File file) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.restore_settings)
                                        .setMessage(R.string.pref_restore_settings_confirm)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (file.exists()) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("file", file);
                                                    getLoaderManager().restartLoader(0, bundle, ImportExportFragment.this);
                                                    progress = ProgressDialogFragment.newInstance(getString(R.string.restoring));
                                                    progress.show(getChildFragmentManager(), "progress");
                                                    handler.setDialog(progress);
                                                }

                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                            }

                            @Override
                            public boolean onDirectorySelected(File file) {
                                return false;
                            }
                        })
                        .show();
                return true;
            }
        });

        ((AlertDialogPreference) findPreference("backup_settings")).setOnPositiveButtonListener(new AlertDialogPreference.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if (PermissionUtils.checkWriteStorage(getActivity())) {
                    File file = new File(BrowserApplication.getExternalUserDirectory(), "backup" + File.separator + FileUtils.getTimeFileName() + EXT);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("file", file);
                    getLoaderManager().restartLoader(1, bundle, ImportExportFragment.this);
                    progress = ProgressDialogFragment.newInstance(getString(R.string.restoring));
                    progress.show(getChildFragmentManager(), "progress");
                    handler.setDialog(progress);
                } else {
                    PermissionUtils.requestStorage(getActivity());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!PermissionUtils.checkWriteStorage(getActivity())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new PermissionDialog().show(getChildFragmentManager(), "permission");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {

        if (id == 0) {
            return new RestoreTask(getActivity(), (File) args.getSerializable("file"));
        } else if (id == 1) {
            return new BackupTask(getActivity(), (File) args.getSerializable("file"));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        handler.sendEmptyMessage(0);

        if (data) {
            Toast.makeText(getActivity(), R.string.succeed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private static DialogHandler handler = new DialogHandler();

    private static class DialogHandler extends Handler {
        private WeakReference<DialogFragment> dialogRef;

        @Override
        public void handleMessage(Message msg) {
            if (dialogRef == null) return;

            DialogFragment dialog = dialogRef.get();
            if (dialog != null) {
                dialog.dismiss();
                dialogRef.clear();
            }

        }

        void setDialog(DialogFragment dialog) {
            dialogRef = new WeakReference<>(dialog);
        }
    }

    public static class PermissionDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.permission_probrem)
                    .setMessage(R.string.confirm_permission_storage)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionUtils.openRequestPermissionSettings(getActivity(),
                                    getString(R.string.request_permission_storage_setting));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().onBackPressed();
                        }
                    });
            setCancelable(false);
            return builder.create();
        }
    }

}
