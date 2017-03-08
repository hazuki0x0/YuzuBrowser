package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.speeddial.WebIcon;
import jp.hazuki.yuzubrowser.speeddial.view.appdata.AppData;
import jp.hazuki.yuzubrowser.speeddial.view.appdata.AppDataListAdapter;
import jp.hazuki.yuzubrowser.speeddial.view.appdata.AppListTask;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragment;

/**
 * Created by hazuki on 16/12/14.
 */

public class SelectShortcutDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<AppData>> {

    private static final int REQUEST_SHORTCUT = 1;
    private static final String APP_ICON = "icon";

    private AppDataListAdapter adapter;
    private ListView listView;
    private ProgressDialogFragment progressDialog;


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
                AppData appData = adapter.getItem(i);
                getArguments().putSerializable(APP_ICON,
                        WebIcon.createIcon(((BitmapDrawable) appData.getIcon()).getBitmap()));
                Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                intent.setClassName(appData.getPackageName(), appData.getClassName());
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
    public Loader<ArrayList<AppData>> onCreateLoader(int i, Bundle args) {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);

        progressDialog = ProgressDialogFragment.newInstance(getString(R.string.now_loading));
        progressDialog.show(getChildFragmentManager(), "progress");
        return new AppListTask(getActivity(), intent, false);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppData>> loader, ArrayList<AppData> data) {
        adapter = new AppDataListAdapter(getActivity(), data);
        listView.setAdapter(adapter);
        if (progressDialog.getDialog() != null) {
            progressDialog.getDialog().dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppData>> loader) {
    }
}
