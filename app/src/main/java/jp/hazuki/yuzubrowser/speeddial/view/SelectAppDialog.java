package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.speeddial.WebIcon;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.appinfo.AppInfo;
import jp.hazuki.yuzubrowser.utils.appinfo.AppInfoListAdapter;
import jp.hazuki.yuzubrowser.utils.appinfo.AppListTask;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragmentCompat;

/**
 * Created by hazuki on 16/12/14.
 */

public class SelectAppDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<AppInfo>> {

    private AppInfoListAdapter adapter;
    private ListView listView;
    private ProgressDialogFragmentCompat progressDialog;

    public static SelectAppDialog newInstance() {
        return new SelectAppDialog();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        listView = new ListView(getActivity());
        builder.setView(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppInfo appInfo = adapter.getItem(i);
                Intent intent = new Intent();
                intent.setClassName(appInfo.getPackageName(), appInfo.getClassName());
                if (getActivity() instanceof SpeedDialSettingActivityController) {
                    WebIcon webIcon = WebIcon.createIcon(ImageUtils.getBitmap(appInfo.getIcon()));
                    SpeedDial speedDial = new SpeedDial(intent.toUri(Intent.URI_INTENT_SCHEME), appInfo.getAppName(), webIcon, false);
                    ((SpeedDialSettingActivityController) getActivity()).goEdit(speedDial);
                }
                dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        getLoaderManager().initLoader(0, null, this);
        return builder.create();
    }

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int i, Bundle args) {
        Intent target = new Intent(Intent.ACTION_MAIN);
        target.addCategory(Intent.CATEGORY_LAUNCHER);

        progressDialog = ProgressDialogFragmentCompat.newInstance(getString(R.string.now_loading));
        progressDialog.show(getChildFragmentManager(), "progress");
        return new AppListTask(getActivity(), target, false);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        adapter = new AppInfoListAdapter(getActivity(), data);
        listView.setAdapter(adapter);
        if (progressDialog.getDialog() != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
    }
}
