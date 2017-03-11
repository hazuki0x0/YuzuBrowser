package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.speeddial.SpeedDialManager;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;


public class SpeedDialSettingActivityFragment extends Fragment implements OnRecyclerListener, FabActionCallBack, SpeedDialEditCallBack {

    private ArrayList<SpeedDial> speedDialList;
    private SpeedDialRecyclerAdapter adapter;
    private SpeedDialManager manager;
    private View rootView;
    private OnSpeedDialAddListener mListener;

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
        if (mListener != null)
            mListener.goEdit(speedDial);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onAdd(int which) {
        if (mListener == null) return;
        switch (which) {
            case 0:
                mListener.goEdit(new SpeedDial());
                break;
            case 1:
                mListener.addFromBookmark();
                break;
            case 2:
                mListener.addFromHistory();
                break;
            case 3:
                mListener.addFromAppList();
                break;
            case 4:
                mListener.addFromShortCutList();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnSpeedDialAddListener) getActivity();
        } catch (ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSpeedDialAddListener {
        void goEdit(SpeedDial speedDial);

        void addFromBookmark();

        void addFromHistory();

        void addFromAppList();

        void addFromShortCutList();
    }
}
