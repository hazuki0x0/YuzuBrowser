package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.view.BookmarkActivity;
import jp.hazuki.yuzubrowser.history.BrowserHistoryActivity;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.speeddial.SpeedDialManager;
import jp.hazuki.yuzubrowser.speeddial.WebIcon;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

/**
 * A placeholder fragment containing a simple view.
 */

public class SpeedDialSettingActivityFragment extends Fragment implements OnRecyclerListener, FabActionCallBack, SpeedDialEditCallBack {

    private static final int RESULT_REQUEST_BOOKMARK = 100;
    private static final int RESULT_REQUEST_HISTORY = 101;

    private ArrayList<SpeedDial> speedDialList;
    private SpeedDialRecyclerAdapter adapter;
    private SpeedDialManager manager;
    private View rootView;

    public SpeedDialSettingActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.recycler_with_fab, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        manager = new SpeedDialManager(getActivity().getApplicationContext());
        speedDialList = manager.getAll();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper helper = new ItemTouchHelper(new ListTouch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        adapter = new SpeedDialRecyclerAdapter(getActivity(), speedDialList, this);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FabActionDialog().show(getChildFragmentManager(), "fab");
            }
        });

        return rootView;
    }

    @Override
    public void onRecyclerClicked(View v, int position) {
        SpeedDial speedDial = speedDialList.get(position);
        if (getActivity() instanceof SpeedDialSettingActivityController) {
            ((SpeedDialSettingActivityController) getActivity()).goEdit(speedDial);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REQUEST_BOOKMARK: {
                if (resultCode != Activity.RESULT_OK || data == null) break;
                String title = data.getStringExtra(Intent.EXTRA_TITLE);
                String url = data.getStringExtra(Intent.EXTRA_TEXT);
                if (getActivity() instanceof SpeedDialSettingActivityController) {
                    ((SpeedDialSettingActivityController) getActivity()).goEdit(new SpeedDial(url, title));
                }
                break;
            }
            case RESULT_REQUEST_HISTORY: {
                if (resultCode != Activity.RESULT_OK || data == null) break;
                String title = data.getStringExtra(Intent.EXTRA_TITLE);
                String url = data.getStringExtra(Intent.EXTRA_TEXT);
                byte[] icon = data.getByteArrayExtra(Intent.EXTRA_STREAM);
                if (getActivity() instanceof SpeedDialSettingActivityController) {
                    SpeedDial speedDial;
                    if (icon == null) {
                        speedDial = new SpeedDial(url, title);
                    } else {
                        speedDial = new SpeedDial(url, title, new WebIcon(icon), true);
                    }
                    ((SpeedDialSettingActivityController) getActivity()).goEdit(speedDial);
                }
                break;
            }
        }
    }

    @Override
    public void onAdd(int which) {
        switch (which) {
            case 0:
                if (getActivity() instanceof SpeedDialSettingActivityController) {
                    ((SpeedDialSettingActivityController) getActivity()).goEdit(new SpeedDial());
                }
                break;
            case 1: {
                Intent intent = new Intent(getActivity(), BookmarkActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, RESULT_REQUEST_BOOKMARK);
                break;
            }
            case 2: {
                Intent intent = new Intent(getActivity(), BrowserHistoryActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, RESULT_REQUEST_HISTORY);
                break;
            }
            case 3:
                SelectAppDialog.newInstance().show(getChildFragmentManager(), "app");
                break;
            case 4:
                SelectShortcutDialog.newInstance().show(getChildFragmentManager(), "shortcut");
                break;
        }
    }

    @Override
    public void onEdited(SpeedDial speedDial) {
        speedDialList.add(speedDial);
        manager.update(speedDial);
        adapter.notifyDataSetChanged();
    }

    public static class FabActionDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.new_speed_dial)
                    .setItems(R.array.new_speed_dial_mode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getParentFragment() instanceof FabActionCallBack) {
                                ((FabActionCallBack) getParentFragment()).onAdd(which);
                            }
                            dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
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
            manager.updateOrder(speedDialList);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final SpeedDial speedDial = speedDialList.remove(position);

            adapter.notifyDataSetChanged();
            Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            speedDialList.add(position, speedDial);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                manager.delete(speedDial.getId());
                            }
                        }
                    })
                    .show();

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    }
}
