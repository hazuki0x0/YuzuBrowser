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

package jp.hazuki.yuzubrowser.adblock.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.adblock.AdBlock;
import jp.hazuki.yuzubrowser.adblock.AdBlockManager;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

public class AdBlockFragment extends Fragment implements OnRecyclerListener, AdBlockEditDialog.AdBlockEditDialogListener,
        AdBlockMenuDialog.OnAdBlockMenuListener, AdBlockItemDeleteDialog.OnBlockItemDeleteListener, ActionMode.Callback,
        DeleteSelectedDialog.OnDeleteSelectedListener, AdBlockDeleteAllDialog.OnDeleteAllListener {
    private static final String ARG_TYPE = "type";
    private static final int REQUEST_SELECT_FILE = 1;
    private static final int REQUEST_SELECT_EXPORT = 2;

    private AdBlockManager.AdBlockItemProvider provider;
    private AdBlockArrayRecyclerAdapter adapter;
    private AdBlockFragmentListener listener;
    private ActionMode actionMode;
    private int type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_ad_block_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        type = getArguments().getInt(ARG_TYPE);
        listener.setFragmentTitle(type);
        provider = AdBlockManager.getProvider(BrowserApplication.getInstance(), type);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        adapter = new AdBlockArrayRecyclerAdapter(getActivity(), provider.getAllItems(), this);
        recyclerView.setAdapter(adapter);

        final FloatingActionMenu fabMenu = (FloatingActionMenu) view.findViewById(R.id.fabMenu);

        view.findViewById(R.id.addByEditFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdBlockEditDialog.newInstance(getString(R.string.add))
                        .show(getChildFragmentManager(), "add");
                fabMenu.close(true);
            }
        });

        view.findViewById(R.id.addFromFileFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, null), REQUEST_SELECT_FILE);
                fabMenu.close(false);
            }
        });
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        AdBlock adBlock = adapter.getItem(position);
        adBlock.setEnable(!adBlock.isEnable());
        provider.update(adBlock);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, int position) {
        AdBlockMenuDialog.newInstance(position, adapter.getItem(position).getId())
                .show(getChildFragmentManager(), "menu");
        return true;
    }

    @Override
    public void onEdited(int index, int id, String text) {
        if (index >= 0 && index < adapter.getItemCount()) {
            AdBlock adBlock = adapter.getItem(index);
            adBlock.setMatch(text);
            provider.update(adBlock);
        } else {
            if (id > -1) {
                for (AdBlock adBlock : adapter.getItems()) {
                    if (adBlock.getId() == id) {
                        adBlock.setMatch(text);
                        provider.update(adBlock);
                        break;
                    }
                }
            } else {
                AdBlock adBlock = new AdBlock(text);
                adapter.getItems().add(adBlock);
                adapter.notifyDataSetChanged();
                provider.update(adBlock);
            }
        }
    }

    public void addAll(List<AdBlock> adBlocks) {
        provider.addAll(adBlocks);
        adapter.getItems().clear();
        adapter.getItems().addAll(provider.getAllItems());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    listener.requestImport(data.getData());
                }
                break;
            case REQUEST_SELECT_EXPORT:
                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    final Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try (OutputStream os = getContext().getContentResolver().openOutputStream(data.getData());
                                 PrintWriter pw = new PrintWriter(os)) {
                                List<AdBlock> adBlockList = provider.getEnableItems();
                                for (AdBlock adBlock : adBlockList)
                                    pw.println(adBlock.getMatch());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), R.string.pref_exported, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).run();
                }
                break;
        }
    }

    @Override
    public void onAskDelete(int index, int id) {
        AdBlockItemDeleteDialog.newInstance(index, id, adapter.getItem(index).getMatch())
                .show(getChildFragmentManager(), "delete");
    }

    @Override
    public void onDelete(int index, int id) {
        AdBlock adBlock = getItem(index, id);
        if (adBlock != null) {
            adapter.getItems().remove(adBlock);
            provider.delete(adBlock.getId());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEdit(int index, int id) {
        AdBlock adBlock = getItem(index, id);
        if (adBlock != null) {
            AdBlockEditDialog.newInstance(getString(R.string.pref_edit), index, adBlock)
                    .show(getChildFragmentManager(), "edit");
        }
    }

    @Override
    public void onResetCount(int index, int id) {
        AdBlock adBlock = getItem(index, id);
        if (adBlock != null) {
            adBlock.setCount(0);
            provider.update(adBlock);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void startMultiSelect(int index) {
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        adapter.setMultiSelectMode(true);
        adapter.setSelect(index, true);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.ad_block_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onDeleteSelected() {
        List<Integer> items = adapter.getSelectedItems();
        Collections.sort(items, Collections.<Integer>reverseOrder());
        for (int index : items) {
            AdBlock adBlock = adapter.getItems().remove(index);
            provider.delete(adBlock.getId());
        }
        actionMode.finish();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                actionMode = mode;
                DeleteSelectedDialog.newInstance()
                        .show(getChildFragmentManager(), "delete_selected");
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.setMultiSelectMode(false);
    }

    private AdBlock getItem(int index, int id) {
        if (index < adapter.getItemCount()) {
            AdBlock adBlock;
            adBlock = adapter.getItem(index);
            if (adBlock.getId() == id)
                return adBlock;
        }
        return getItemFromId(id);
    }

    private AdBlock getItemFromId(int id) {
        for (AdBlock adBlock : adapter.getItems())
            if (adBlock.getId() == id)
                return adBlock;
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ad_block_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_TITLE, listener.getExportFileName(type));
                startActivityForResult(intent, REQUEST_SELECT_EXPORT);
                return true;
            case R.id.deleteAll:
                AdBlockDeleteAllDialog.newInstance()
                        .show(getChildFragmentManager(), "delete_all");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteAll() {
        provider.deleteAll();
        adapter.getItems().clear();
        adapter.notifyDataSetChanged();
    }

    public static AdBlockFragment newInstance(int type) {
        AdBlockFragment fragment = new AdBlockFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AdBlockFragmentListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface AdBlockFragmentListener {
        void setFragmentTitle(int type);

        void requestImport(Uri uri);

        String getExportFileName(int type);
    }
}