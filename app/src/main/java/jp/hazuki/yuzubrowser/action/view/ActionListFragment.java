package jp.hazuki.yuzubrowser.action.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;
import jp.hazuki.yuzubrowser.utils.view.recycler.RecyclerFabFragment;
import jp.hazuki.yuzubrowser.utils.view.recycler.SimpleViewHolder;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hazuki on 17/02/26.
 */

public class ActionListFragment extends RecyclerFabFragment implements OnRecyclerListener {
    public static final String EXTRA_ACTION_LIST = "ActionListActivity.extra.actionList";
    private static final String EXTRA_POSITION = "pos";
    private static final int RESULT_REQUEST_ADD = 1;
    private static final int RESULT_REQUEST_EDIT = 2;
    private static final int RESULT_REQUEST_ADD_EASY = 3;

    private ActionList mList;
    private ActionListAdapter adapter;
    private ActionNameArray mActionNameArray;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        mActionNameArray = getArguments().getParcelable(ActionNameArray.INTENT_EXTRA);

        ActionList actions = getArguments().getParcelable(EXTRA_ACTION_LIST);
        setActionList(actions);

        return getRootView();
    }

    @Override
    public void onRecyclerClicked(View v, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_POSITION, position);
        Intent intent = new ActionActivity.Builder(getActivity())
                .setDefaultAction(mList.get(position))
                .setActionNameArray(mActionNameArray)
                .setTitle(R.string.edit_action)
                .setReturnData(bundle)
                .create();
        startActivityForResult(intent, RESULT_REQUEST_EDIT);
    }

    @Override
    protected void onAddButtonClick() {
        Intent intent = new ActionActivity.Builder(getActivity())
                .setTitle(R.string.edit_action)
                .setActionNameArray(mActionNameArray)
                .create();

        startActivityForResult(intent, RESULT_REQUEST_ADD);
    }

    @Override
    protected boolean onAddButtonLongClick() {
        startEasyAdd();
        return true;
    }

    protected void setActionList(ActionList list) {
        if (list == null)
            mList = new ActionList();
        else
            mList = list;

        adapter = new ActionListAdapter(getActivity(), mList, mActionNameArray, this);
        setRecyclerViewAdapter(adapter);
    }

    protected void onActionListChanged() {
        if (getActivity() instanceof ActionListActivity) {
            ((ActionListActivity) getActivity()).onActionListChanged(mList);
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_ACTION_LIST, (Parcelable) mList);
        getActivity().setResult(RESULT_OK, data);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, int item1, int item2) {
        adapter.move(item1, item2);
        onActionListChanged();
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, final int index) {
        final Action action = mList.remove(index);
        adapter.notifyDataSetChanged();
        onActionListChanged();
        Snackbar.make(getRootView(), R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mList.add(index, action);
                        adapter.notifyDataSetChanged();
                        onActionListChanged();
                    }
                })
                .show();
    }

    private void startEasyAdd() {
        Intent intent = new ActionActivity.Builder(getActivity())
                .setTitle(R.string.action_easy_add)
                .setActionNameArray(mActionNameArray)
                .create();

        startActivityForResult(intent, RESULT_REQUEST_ADD_EASY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(R.string.action_to_json).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getActivity(), ActionStringActivity.class);
                intent.putExtra(ActionStringActivity.EXTRA_ACTION, (Parcelable) mList);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            Action action = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
            switch (requestCode) {
                case RESULT_REQUEST_ADD:
                    mList.add(action);
                    break;
                case RESULT_REQUEST_EDIT:
                    if (action.isEmpty()) {
                        Snackbar.make(getRootView(), R.string.action_cant_empty, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    int position = data.getBundleExtra(ActionActivity.EXTRA_RETURN).getInt(EXTRA_POSITION);
                    mList.set(position, action);
                    break;
                case RESULT_REQUEST_ADD_EASY:
                    for (SingleAction singleaction : action) {
                        mList.add(new Action(singleaction));
                    }
                    break;
            }

            onActionListChanged();
            adapter.notifyDataSetChanged();
        }

    }

    public static Fragment newInstance(ActionList actionList, ActionNameArray nameArray) {
        Fragment fragment = new ActionListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ACTION_LIST, actionList);
        bundle.putParcelable(ActionNameArray.INTENT_EXTRA, nameArray);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static class ActionListAdapter extends ArrayRecyclerAdapter<Action, SimpleViewHolder> {

        private ActionNameArray nameList;

        ActionListAdapter(Context context, ActionList actionList, ActionNameArray nameArray, OnRecyclerListener recyclerListener) {
            super(context, actionList, recyclerListener);
            nameList = nameArray;
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, Action action, int position) {
            holder.textView.setText(action.toString(nameList));
        }

        @Override
        protected SimpleViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new SimpleViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false),
                    android.R.id.text1);
        }
    }
}
