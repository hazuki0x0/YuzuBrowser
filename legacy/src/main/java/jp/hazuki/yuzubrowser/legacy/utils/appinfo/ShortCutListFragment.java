package jp.hazuki.yuzubrowser.legacy.utils.appinfo;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public class ShortCutListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<AppInfo>> {
    private static final int REQUEST_SHORTCUT = 1;

    private OnShortCutSelectListener mListener;
    private AppInfoListAdapter adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AppInfo appInfo = adapter.getItem(position);
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        intent.setClassName(appInfo.getPackageName(), appInfo.getClassName());
        startActivityForResult(intent, REQUEST_SHORTCUT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SHORTCUT:
                if (resultCode == Activity.RESULT_OK) {
                    if (mListener != null)
                        mListener.onShortCutSelected(data);
                }
        }
    }

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        return new AppListTask(getActivity(), intent, false);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        adapter = new AppInfoListAdapter(getActivity(), data);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnShortCutSelectListener) getActivity();
        } catch (ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnShortCutSelectListener {
        void onShortCutSelected(Intent data);
    }
}
