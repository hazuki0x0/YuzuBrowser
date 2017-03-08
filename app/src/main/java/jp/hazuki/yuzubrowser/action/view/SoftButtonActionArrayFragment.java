package jp.hazuki.yuzubrowser.action.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionManager;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.SoftButtonActionArrayManagerBase;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionArrayFile;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;
import jp.hazuki.yuzubrowser.utils.view.recycler.RecyclerFabFragment;
import jp.hazuki.yuzubrowser.utils.view.recycler.SimpleViewHolder;

/**
 * Created by hazuki on 17/02/28.
 */

public class SoftButtonActionArrayFragment extends RecyclerFabFragment implements OnRecyclerListener {
    private static final String ACTION_TYPE = "type";
    private static final String ACTION_ID = "id";
    private static final int RESULT_REQUEST_ADD = 1;

    private int mActionType;
    private int mActionId;
    private SoftButtonActionArrayFile mActionArray;
    private SoftButtonActionArrayManagerBase mActionManager;
    private ActionListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initData();
        ActionNameArray mActionNameArray = new ActionNameArray(getActivity());
        adapter = new ActionListAdapter(getActivity(), mActionArray.list, mActionNameArray, this);
        setRecyclerViewAdapter(adapter);
        checkMax();
        return getRootView();
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, int fromIndex, int toIndex) {
        adapter.move(fromIndex, toIndex);
        return true;
    }

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        mActionArray.write(BrowserApplication.getInstance());
    }

    @Override
    public void onRecyclerClicked(View v, int position) {
        onListItemClick(position);
    }

    @Override
    protected void onAddButtonClick() {
        mActionArray.list.add(new SoftButtonActionFile());
        mActionArray.write(BrowserApplication.getInstance());
        onListItemClick(mActionArray.list.size() - 1);
    }

    private void onListItemClick(int position) {
        Intent intent = new SoftButtonActionActivity.Builder(getActivity())
                .setActionManager(mActionType, mActionManager.makeActionIdFromPosition(mActionId, position))
                .setTitle(getActivity().getTitle() + " - " + String.valueOf(position + 1))
                .create();
        startActivityForResult(intent, RESULT_REQUEST_ADD);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, final int index) {
        final SoftButtonActionFile file = mActionArray.list.remove(index);
        adapter.notifyDataSetChanged();
        checkMax();
        Snackbar.make(getRootView(), R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActionArray.list.add(index, file);
                        adapter.notifyDataSetChanged();
                        checkMax();
                    }
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            mActionArray.write(BrowserApplication.getInstance());
                        }
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REQUEST_ADD:
                adapter.notifyDataSetChanged();
                checkMax();
                break;
        }
    }

    private void checkMax() {
        setAddButtonEnabled(mActionManager.getMax() >= adapter.getItemCount());
    }

    private void initData() {
        mActionType = getArguments().getInt(ACTION_TYPE);
        mActionId = getArguments().getInt(ACTION_ID);

        ActionManager action_manager = ActionManager.getActionManager(BrowserApplication.getInstance(), mActionType);

        if (!(action_manager instanceof SoftButtonActionArrayManagerBase))
            throw new IllegalArgumentException();

        mActionManager = (SoftButtonActionArrayManagerBase) action_manager;
        mActionArray = mActionManager.getActionArrayFile(mActionId);
    }

    public static Fragment newInstance(int actionType, int actionId) {
        Fragment fragment = new SoftButtonActionArrayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ACTION_TYPE, actionType);
        bundle.putInt(ACTION_ID, actionId);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static class ActionListAdapter extends ArrayRecyclerAdapter<SoftButtonActionFile, SimpleViewHolder> {
        private ActionNameArray actionNameArray;


        ActionListAdapter(Context context, List<SoftButtonActionFile> list, ActionNameArray array, OnRecyclerListener listener) {
            super(context, list, listener);
            actionNameArray = array;
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, SoftButtonActionFile item, int position) {
            holder.textView.setText(item.press.toString(actionNameArray));
        }

        @Override
        protected SimpleViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new SimpleViewHolder(
                    inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false),
                    android.R.id.text1);
        }


    }
}
