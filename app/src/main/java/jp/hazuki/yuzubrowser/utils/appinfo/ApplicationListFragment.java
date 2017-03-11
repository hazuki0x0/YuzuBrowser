package jp.hazuki.yuzubrowser.utils.appinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class ApplicationListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<AppInfo>> {
    private static final String TYPE = "type";
    private static final String INTENT = "Intent";

    private AppInfoListAdapter adapter;
    private OnAppSelectListener mListener;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener != null) {
            AppInfo appInfo = adapter.getItem(position);
            mListener.onAppSelected(getArguments().getInt(TYPE, -1), appInfo);
        }
    }

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        Intent target = getArguments().getParcelable(INTENT);

        return new AppListTask(getActivity(), target, false);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> data) {
        adapter = new AppInfoListAdapter(getActivity(), data);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {

    }

    public static ApplicationListFragment newInstance(@NonNull Intent intent) {
        ApplicationListFragment fragment = new ApplicationListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(INTENT, intent);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ApplicationListFragment newInstance(int type, @NonNull Intent intent) {
        ApplicationListFragment fragment = new ApplicationListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        bundle.putParcelable(INTENT, intent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnAppSelectListener) getActivity();
        } catch (ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAppSelectListener {
        void onAppSelected(int type, AppInfo info);
    }
}
