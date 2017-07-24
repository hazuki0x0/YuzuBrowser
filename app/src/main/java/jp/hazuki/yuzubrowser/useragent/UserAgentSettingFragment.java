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

package jp.hazuki.yuzubrowser.useragent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

import static jp.hazuki.yuzubrowser.useragent.SelectActionDialog.DELETE;
import static jp.hazuki.yuzubrowser.useragent.SelectActionDialog.EDIT;

public class UserAgentSettingFragment extends Fragment implements DeleteUserAgentDialog.OnDelete, EditUserAgentDialog.OnEditedUserAgent, SelectActionDialog.OnActionSelect, OnRecyclerListener {
    private UserAgentList mUserAgentList;
    private UserAgentRecyclerAdapter mAdapter;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.recycler_with_fab, container, false);
        setHasOptionsMenu(true);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mUserAgentList = new UserAgentList();
        mUserAgentList.read(getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper helper = new ItemTouchHelper(new ListTouch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        mAdapter = new UserAgentRecyclerAdapter(getActivity(), mUserAgentList, this);
        recyclerView.setAdapter(mAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditUserAgentDialog.newInstance().show(getChildFragmentManager(), "new");
            }
        });
        return rootView;
    }

    @Override
    public void onDelete(int position) {
        mAdapter.remove(position);
        mUserAgentList.write(getActivity());
    }

    @Override
    public void onEdited(int position, String name, String ua) {
        if (position < 0) {
            mUserAgentList.add(new UserAgent(name, ua));
            mUserAgentList.write(getActivity());
            mAdapter.notifyItemInserted(mAdapter.size() - 1);
        } else {
            UserAgent userAgent = mUserAgentList.get(position);
            userAgent.name = name;
            userAgent.useragent = ua;
            mUserAgentList.set(position, userAgent);
            mUserAgentList.write(getActivity());
            mAdapter.notifyItemInserted(position);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                boolean next = !mAdapter.isSortMode();
                mAdapter.setSortMode(next);

                Toast.makeText(getActivity(), (next) ? R.string.start_sort : R.string.end_sort, Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onActionSelected(@SelectActionDialog.ActionMode int mode, int position, UserAgent userAgent) {
        switch (mode) {
            case EDIT:
                EditUserAgentDialog.newInstance(position, userAgent)
                        .show(getChildFragmentManager(), "edit");
                break;
            case DELETE:
                DeleteUserAgentDialog.newInstance(position)
                        .show(getChildFragmentManager(), "delete");
                break;
        }
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        EditUserAgentDialog.newInstance(position, mUserAgentList.get(position))
                .show(getChildFragmentManager(), "edit");
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, int position) {
        SelectActionDialog.newInstance(position, mUserAgentList.get(position))
                .show(getChildFragmentManager(), "action");
        return true;
    }

    private class ListTouch extends ItemTouchHelper.Callback {


        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mUserAgentList.write(getActivity());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final UserAgent ua = mAdapter.remove(position);

            Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAdapter.add(position, ua);
                            mAdapter.notifyItemInserted(position);
                        }
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event != DISMISS_EVENT_ACTION && event != DISMISS_EVENT_MANUAL) {
                                mUserAgentList.write(getActivity());
                            }
                        }
                    })
                    .show();
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return mAdapter.isSortMode();
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    }
}
