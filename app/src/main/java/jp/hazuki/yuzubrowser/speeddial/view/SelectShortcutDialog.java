package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
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
import jp.hazuki.yuzubrowser.utils.appinfo.AppInfo;
import jp.hazuki.yuzubrowser.utils.appinfo.AppInfoListAdapter;
import jp.hazuki.yuzubrowser.utils.appinfo.AppListTask;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragmentCompat;

/**
 * Created by hazuki on 16/12/14.
 */

public class SelectShortcutDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<AppInfo>> {

    private static final int REQUEST_SHORTCUT = 1;
    private static final String APP_ICON = "icon";

    private AppInfoListAdapter adapter;
    private ListView listView;
    private ProgressDialogFragmentCompat progressDialog;


    public static DialogFragment newInstance() {
        DialogFragment fragment = new SelectShortcutDialog();
        fragment.setArguments(new Bundle());
        return fragment;
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
                getArguments().putSerializable(APP_ICON,
                        WebIcon.createIcon(((BitmapDrawable) appInfo.getIcon()).getBitmap()));
                Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                intent.setClassName(appInfo.getPackageName(), appInfo.getClassName());
                startActivityForResult(intent, REQUEST_SHORTCUT);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        getLoaderManager().initLoader(0, null, this);
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SHORTCUT) {
            if (getActivity() instanceof SpeedDialSettingActivityController) {
                Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                WebIcon webIcon = (WebIcon) getArguments().getSerializable(APP_ICON);
                SpeedDial speedDial = new SpeedDial(intent.toUri(Intent.URI_INTENT_SCHEME), name, webIcon, false);
                ((SpeedDialSettingActivityController) getActivity()).goEdit(speedDial);
            }
            //dismiss();
        }
    }

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int i, Bundle args) {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);

        progressDialog = ProgressDialogFragmentCompat.newInstance(getString(R.string.now_loading));
        progressDialog.show(getChildFragmentManager(), "progress");
        return new AppListTask(getActivity(), intent, false);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        adapter = new AppInfoListAdapter(getActivity(), data);
        listView.setAdapter(adapter);
        if (progressDialog.getDialog() != null) {
            progressDialog.getDialog().dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
    }
}
