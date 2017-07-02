/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.gesture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.view.DeleteDialogCompat;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;
import jp.hazuki.yuzubrowser.utils.view.recycler.RecyclerFabFragment;

public class GestureListFragment extends RecyclerFabFragment implements OnRecyclerListener, DeleteDialogCompat.OnDelete {
    private static final int RESULT_REQUEST_ADD = 0;
    private static final int RESULT_REQUEST_EDIT = 1;
    private static final String ITEM_ID = "id";

    private GestureManager mManager;
    private int mGestureId;
    private GestureListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        dataInit();
        return getRootView();
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        GestureItem item = adapter.getItems().get(position);
        Bundle bundle = new Bundle();
        bundle.putLong(ITEM_ID, item.getId());
        Intent intent = new ActionActivity.Builder(getActivity())
                .setDefaultAction(item.getAction())
                .setTitle(R.string.action_settings)
                .setReturnData(bundle)
                .create();

        startActivityForResult(intent, RESULT_REQUEST_EDIT);
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, int position) {
        DeleteDialogCompat.newInstance(getActivity(), R.string.confirm, R.string.confirm_delete_gesture, position)
                .show(getChildFragmentManager(), "delete");
        return true;
    }

    @Override
    public void onDelete(int position) {
        GestureItem item = adapter.remove(position);
        mManager.remove(item);
        reset();
    }

    @Override
    protected void onAddButtonClick() {
        Intent intent = new Intent(getActivity(), AddGestureActivity.class);
        intent.putExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, mGestureId);
        intent.putExtra(Intent.EXTRA_TITLE, getActivity().getTitle());
        startActivityForResult(intent, RESULT_REQUEST_ADD);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, int fromIndex, int toIndex) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RESULT_REQUEST_ADD:
                    reset();
                    break;
                case RESULT_REQUEST_EDIT:
                    if (data == null)
                        break;
                    Bundle bundle = data.getBundleExtra(ActionActivity.EXTRA_RETURN);
                    Action action = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                    mManager.updateAction(bundle.getLong(ITEM_ID), action);
                    reset();
                    break;
            }
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, final int index) {
        final GestureItem item = adapter.getItems().remove(index);
        adapter.notifyDataSetChanged();
        Snackbar.make(getRootView(), R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.getItems().add(index, item);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION && event != DISMISS_EVENT_MANUAL) {
                            mManager.remove(item);
                            reset();
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    private void dataInit() {
        ActionNameArray mActionNameArray = getArguments().getParcelable(ActionNameArray.INTENT_EXTRA);
        mGestureId = getArguments().getInt(GestureManager.INTENT_EXTRA_GESTURE_ID);
        mManager = GestureManager.getInstance(getActivity().getApplicationContext(), mGestureId);
        mManager.load();
        adapter = new GestureListAdapter(getActivity(), mManager.getList(), mActionNameArray, this);
        setRecyclerViewAdapter(adapter);
    }

    private void reset() {
        mManager.load();
        adapter.getItems().clear();
        adapter.getItems().addAll(mManager.getList());
        adapter.notifyDataSetChanged();
    }

    public static Fragment newInstance(int gestureId, ActionNameArray nameArray) {
        Fragment fragment = new GestureListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(GestureManager.INTENT_EXTRA_GESTURE_ID, gestureId);
        bundle.putParcelable(ActionNameArray.INTENT_EXTRA, nameArray);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static class GestureListAdapter extends ArrayRecyclerAdapter<GestureItem, GestureListAdapter.ViewHolder> {

        private ActionNameArray nameList;
        private int size;
        private int color;

        GestureListAdapter(Context context, List<GestureItem> actionList, ActionNameArray nameArray, OnRecyclerListener recyclerListener) {
            super(context, actionList, recyclerListener);
            nameList = nameArray;
            size = (int) context.getResources().getDimension(R.dimen.gesture_image_size);
            color = ResourcesCompat.getColor(context.getResources(), R.color.add_gesture_color, context.getTheme());
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, GestureItem item, int position) {
            holder.imageView.setImageBitmap(item.getBitmap(size, size, color));
            Action action = item.getAction();

            if (action == null || action.isEmpty())
                holder.title.setText(R.string.action_empty);
            else
                holder.title.setText(action.toString(nameList));
        }

        @Override
        protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.image_text_list_item, parent, false));
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView title;
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.textView);
                imageView = (ImageView) itemView.findViewById(R.id.imageView);
            }
        }
    }
}
