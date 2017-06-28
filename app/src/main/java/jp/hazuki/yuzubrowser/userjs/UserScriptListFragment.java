/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.userjs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.IOUtils;
import jp.hazuki.yuzubrowser.utils.view.DeleteDialogCompat;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListActivity;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

import static android.app.Activity.RESULT_OK;

public class UserScriptListFragment extends Fragment implements OnUserJsItemClickListener, DeleteDialogCompat.OnDelete {
    private static final int REQUEST_ADD_USERJS = 1;
    private static final int REQUEST_EDIT_USERJS = 2;
    private static final int REQUEST_ADD_FROM_FILE = 3;
    private UserScriptDatabase mDb;
    private UserJsAdapter adapter;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_script_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        ItemTouchHelper helper = new ItemTouchHelper(new ListTouch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);

        final FloatingActionMenu fabMenu = (FloatingActionMenu) rootView.findViewById(R.id.fabMenu);

        final FloatingActionButton sortFab = (FloatingActionButton) rootView.findViewById(R.id.sortFab);

        sortFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                boolean next = !adapter.isSortMode();
                adapter.setSortMode(next);

                Toast.makeText(getActivity(), (next) ? R.string.start_sort : R.string.end_sort, Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sortFab.setLabelText(adapter.isSortMode() ? getString(R.string.end_sort_label) : getString(R.string.sort));
                    }
                }, 500);
            }
        });

        rootView.findViewById(R.id.addByEditFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserScriptEditActivity.class);
                startActivityForResult(intent, REQUEST_ADD_USERJS);
                fabMenu.close(false);
            }
        });

        rootView.findViewById(R.id.addFromFileFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FileListActivity.class);
                intent.putExtra(FileListActivity.EXTRA_FILE, Environment.getExternalStorageDirectory());
                startActivityForResult(intent, REQUEST_ADD_FROM_FILE);
                fabMenu.close(false);
            }
        });

        mDb = new UserScriptDatabase(getActivity().getApplicationContext());
        List<UserScript> scripts = mDb.getAllList();
        adapter = new UserJsAdapter(getActivity(), scripts, this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    private void reset() {
        adapter.getItems().clear();
        adapter.getItems().addAll(mDb.getAllList());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        Intent intent = new Intent(getActivity(), UserScriptEditActivity.class);
        intent.putExtra(UserScriptEditActivity.EXTRA_USERSCRIPT, adapter.getItems().get(position));
        startActivityForResult(intent, REQUEST_EDIT_USERJS);
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, final int position) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        Menu menu = popupMenu.getMenu();

        menu.add(R.string.userjs_info).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onInfoButtonClick(null, position);
                return false;
            }
        });

        menu.add(R.string.userjs_edit).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getActivity(), UserScriptEditActivity.class);
                intent.putExtra(UserScriptEditActivity.EXTRA_USERSCRIPT, adapter.getItems().get(position));
                startActivityForResult(intent, REQUEST_EDIT_USERJS);
                return false;
            }
        });

        menu.add(R.string.userjs_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DeleteDialogCompat.newInstance(getActivity(), R.string.confirm, R.string.userjs_delete_confirm, position)
                        .show(getChildFragmentManager(), "delete");
                return false;
            }
        });

        popupMenu.show();
        return true;
    }

    @Override
    public void onDelete(int position) {
        UserScript js = adapter.remove(position);
        adapter.notifyDataSetChanged();
        mDb.delete(js);
    }

    @Override
    public void onInfoButtonClick(View v, int index) {
        InfoDialog.newInstance(adapter.getItems().get(index))
                .show(getChildFragmentManager(), "info");
    }

    @Override
    public void onCheckBoxClicked(View v, int index) {
        UserScript script = adapter.getItems().get(index);
        script.setEnabled(!script.isEnabled());
        mDb.update(script);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_USERJS: {
                if (resultCode != RESULT_OK || data == null)
                    break;
                UserScript js = data.getParcelableExtra(UserScriptEditActivity.EXTRA_USERSCRIPT);
                if (js == null)
                    throw new NullPointerException("UserJs is null");
                mDb.add(js);
                reset();
            }
            break;
            case REQUEST_EDIT_USERJS: {
                if (resultCode != RESULT_OK || data == null)
                    break;
                UserScript js = data.getParcelableExtra(UserScriptEditActivity.EXTRA_USERSCRIPT);
                if (js == null)
                    throw new NullPointerException("UserJs is null");
                mDb.update(js);
                reset();
            }
            break;
            case REQUEST_ADD_FROM_FILE: {
                if (resultCode != RESULT_OK || data == null)
                    break;
                final File file = (File) data.getSerializableExtra(FileListActivity.EXTRA_FILE);
                if (file == null)
                    throw new NullPointerException("file is null");
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(String.format(getString(R.string.userjs_add_file_confirm), file.getName()))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String data = IOUtils.readFile(file, "UTF-8");
                                    mDb.add(new UserScript(data));
                                    reset();
                                } catch (IOException e) {
                                    ErrorReport.printAndWriteLog(e);
                                    Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
            break;
        }
    }

    private class ListTouch extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            adapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mDb.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int index = viewHolder.getAdapterPosition();
            final UserScript js = adapter.getItems().remove(index);
            adapter.notifyDataSetChanged();
            Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.getItems().add(index, js);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                mDb.delete(js);
                            }
                        }
                    })
                    .show();
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return adapter.isSortMode();
        }
    }

    public static class InfoDialog extends DialogFragment {
        private static final String USER_SCRIPT = "js";

        public static DialogFragment newInstance(UserScript script) {
            DialogFragment dialog = new InfoDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelable(USER_SCRIPT, script);
            dialog.setArguments(bundle);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            UserScript js = getArguments().getParcelable(USER_SCRIPT);
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.userjs_info_dialog, null);
            ((TextView) view.findViewById(R.id.nameTextView)).setText(js.getName());
            ((TextView) view.findViewById(R.id.versionTextView)).setText(js.getVersion());
            ((TextView) view.findViewById(R.id.authorTextView)).setText(js.getAuthor());
            ((TextView) view.findViewById(R.id.descriptionTextView)).setText(js.getDescription());
            ((TextView) view.findViewById(R.id.includeTextView)).setText(ArrayUtils.join(js.getInclude(), "\n"));
            ((TextView) view.findViewById(R.id.excludeTextView)).setText(ArrayUtils.join(js.getExclude(), "\n"));

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.userjs_info)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();

        }
    }

    private static class UserJsAdapter extends ArrayRecyclerAdapter<UserScript, UserJsAdapter.ViewHolder> {

        UserJsAdapter(Context context, List<UserScript> list, OnRecyclerListener listener) {
            super(context, list, listener);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, UserScript item, int position) {
            holder.textView.setText(item.getName());
            holder.checkBox.setChecked(item.isEnabled());
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OnUserJsItemClickListener) getListener())
                            .onInfoButtonClick(v, holder.getAdapterPosition());
                }
            });
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OnUserJsItemClickListener) getListener())
                            .onCheckBoxClicked(v, holder.getAdapterPosition());
                }
            });
        }

        @Override
        protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.fragment_userjs_item, parent, false));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageButton button;
            CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.textView);
                button = (ImageButton) itemView.findViewById(R.id.infoButton);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            }
        }
    }
}
