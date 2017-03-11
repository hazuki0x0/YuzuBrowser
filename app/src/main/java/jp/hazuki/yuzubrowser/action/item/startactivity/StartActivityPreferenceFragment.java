package jp.hazuki.yuzubrowser.action.item.startactivity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/03/11.
 */

public class StartActivityPreferenceFragment extends ListFragment {

    private OnActionListener mListener;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.action_start_activity_template));
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0://Application
                if (mListener != null)
                    mListener.openApplicationList();
                break;
            case 1://Shortcut
                if (mListener != null)
                    mListener.openShortCutList();
                break;
            case 2://Share page
                if (mListener != null)
                    mListener.openSharePage();
                break;
            case 3://Open in other app
                if (mListener != null)
                    mListener.openOther();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnActionListener) getActivity();
        } catch (ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnActionListener {
        void openApplicationList();

        void openShortCutList();

        void openSharePage();

        void openOther();
    }
}
