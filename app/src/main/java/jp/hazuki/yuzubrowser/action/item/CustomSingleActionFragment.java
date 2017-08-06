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

package jp.hazuki.yuzubrowser.action.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;
import jp.hazuki.yuzubrowser.utils.view.recycler.RecyclerMenu;

public class CustomSingleActionFragment extends Fragment implements OnRecyclerListener, RecyclerMenu.OnRecyclerMenuListener {
    private static final int RESULT_REQUEST_PREFERENCE = 1;
    private static final int RESULT_REQUEST_EDIT = 2;

    private static final String ARG_ACTION = "action";
    private static final String ARG_NAME = "name";

    private static final String ARG_POSITION = "position";

    private ActionAdapter adapter;
    private ActionNameArray actionNameArray;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.action_custom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        rootView = view;
        final EditText editText = view.findViewById(R.id.editText);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        ItemTouchHelper helper = new ItemTouchHelper(new Touch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);

        Action actions = getArguments().getParcelable(ARG_ACTION);
        String name = getArguments().getString(ARG_NAME);
        actionNameArray = getArguments().getParcelable(ActionNameArray.INTENT_EXTRA);

        if (actionNameArray == null) {
            actionNameArray = new ActionNameArray(getActivity());
        }

        adapter = new ActionAdapter(getActivity(), actions, actionNameArray, this, this);
        adapter.setSortMode(true);
        recyclerView.setAdapter(adapter);
        editText.setText(name);

        view.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editText.getText().toString();

                if (TextUtils.isEmpty(name) && adapter.getItemCount() > 0) {
                    name = adapter.get(0).toString(actionNameArray);
                }

                Intent result = new Intent();
                result.putExtra(CustomSingleActionActivity.EXTRA_NAME, name);
                result.putExtra(CustomSingleActionActivity.EXTRA_ACTION, (Parcelable) new Action(adapter.getItems()));
                getActivity().setResult(Activity.RESULT_OK, result);
                getActivity().finish();
            }
        });

        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        view.findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new ActionActivity.Builder(getActivity())
                        .setTitle(R.string.add)
                        .setActionNameArray(actionNameArray)
                        .create();

                startActivityForResult(intent, RESULT_REQUEST_PREFERENCE);
            }
        });
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION, position);
        Intent intent = new ActionActivity.Builder(getActivity())
                .setTitle(R.string.edit_action)
                .setDefaultAction(new Action(adapter.get(position)))
                .setActionNameArray(actionNameArray)
                .setReturnData(bundle)
                .create();

        startActivityForResult(intent, RESULT_REQUEST_EDIT);
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, int position) {
        return false;
    }

    @Override
    public void onDelete(int position) {
        adapter.remove(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REQUEST_PREFERENCE: {
                Action action = ActionActivity.getActionFromIntent(resultCode, data);
                if (action == null) {
                    return;
                }
                adapter.addAll(action);
                adapter.notifyItemInserted(adapter.getItemCount() - 1);
            }
            break;
            case RESULT_REQUEST_EDIT: {
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                Action action = ActionActivity.getActionFromIntent(resultCode, data);
                Bundle returnData = ActionActivity.getReturnData(data);
                if (action == null || returnData == null) {
                    return;
                }
                int position = returnData.getInt(ARG_POSITION);
                if (action.size() == 1) {
                    adapter.set(position, action.get(0));
                    adapter.notifyItemChanged(position);
                } else {
                    adapter.remove(position);
                    for (int i = action.size() - 1; i >= 0; i--) {
                        adapter.add(position, action.get(i));
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public static CustomSingleActionFragment newInstance(Action actionList, String name, ActionNameArray actionNameArray) {
        CustomSingleActionFragment fragment = new CustomSingleActionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_ACTION, actionList);
        bundle.putString(ARG_NAME, name);
        bundle.putParcelable(ActionNameArray.INTENT_EXTRA, actionNameArray);
        fragment.setArguments(bundle);
        return fragment;
    }

    private class Touch extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            adapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final SingleAction action = adapter.remove(position);

            Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.add(position, action);
                            adapter.notifyItemInserted(position);
                        }
                    })
                    .show();
        }
    }

    private static class ActionAdapter extends ArrayRecyclerAdapter<SingleAction, ActionAdapter.AVH> implements RecyclerMenu.OnRecyclerMoveListener {

        private ActionNameArray nameArray;
        private Context context;
        private RecyclerMenu.OnRecyclerMenuListener menuListener;

        ActionAdapter(Context context, Action list, ActionNameArray actionNameArray, RecyclerMenu.OnRecyclerMenuListener recyclerMenuListener, OnRecyclerListener listener) {
            super(context, list, listener);
            nameArray = actionNameArray;
            this.context = context;
            menuListener = recyclerMenuListener;
        }

        @Override
        public void onBindViewHolder(final AVH holder, SingleAction item, final int position) {
            holder.title.setText(item.toString(nameArray));
            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new RecyclerMenu(context, v, holder.getAdapterPosition(), menuListener, ActionAdapter.this).show();
                }
            });
        }

        @Override
        protected AVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new AVH(inflater.inflate(R.layout.action_custom_item, parent, false), this);
        }

        @Override
        public void onMoveUp(int position) {
            if (position > 0) {
                move(position, position - 1);
            }
        }

        @Override
        public void onMoveDown(int position) {
            if (position < getItemCount() - 1) {
                move(position, position + 1);
            }
        }

        static class AVH extends ArrayRecyclerAdapter.ArrayViewHolder<SingleAction> {

            private TextView title;
            private ImageButton menu;

            AVH(View itemView, ActionAdapter adapter) {
                super(itemView, adapter);

                title = itemView.findViewById(R.id.titleTextView);
                menu = itemView.findViewById(R.id.menu);
            }
        }
    }
}
