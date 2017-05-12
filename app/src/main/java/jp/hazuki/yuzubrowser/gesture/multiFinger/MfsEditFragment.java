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

package jp.hazuki.yuzubrowser.gesture.multiFinger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.gesture.multiFinger.data.MultiFingerGestureItem;
import jp.hazuki.yuzubrowser.gesture.multiFinger.detector.MultiFingerGestureDetector;
import jp.hazuki.yuzubrowser.utils.view.SpinnerButton;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

public class MfsEditFragment extends Fragment {
    private static final String ARG_INDEX = "index";
    private static final String ARG_ITEM = "item";
    private static final int REQUEST_ACTION = 1;

    private OnMfsEditFragmentListener listener;
    private MultiFingerGestureItem item;
    private ActionNameArray nameArray;
    private SpinnerButton actionButton;
    private MfsFingerAdapter adapter;
    private RecyclerView recyclerView;

    public static MfsEditFragment newInstance(int index, MultiFingerGestureItem item) {
        MfsEditFragment fragment = new MfsEditFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INDEX, index);
        bundle.putParcelable(ARG_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_multi_finger_edit, container, false);

        item = getArguments().getParcelable(ARG_ITEM);
        if (item == null)
            item = new MultiFingerGestureItem();
        nameArray = new ActionNameArray(getActivity());

        actionButton = (SpinnerButton) v.findViewById(R.id.actionButton);

        String text = item.getAction().toString(nameArray);
        actionButton.setText(text != null ? text : getText(R.string.action_empty));
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new ActionActivity.Builder(getActivity())
                        .setDefaultAction(item.getAction())
                        .setTitle(R.string.pref_multi_finger_gesture_settings)
                        .create();
                startActivityForResult(intent, REQUEST_ACTION);
            }
        });

        SeekBar seekBar = (SeekBar) v.findViewById(R.id.fingerSeekBar);
        final TextView seekTextView = (TextView) v.findViewById(R.id.seekTextView);
        seekBar.setProgress(item.getFingers() - 1);
        seekTextView.setText(Integer.toString(item.getFingers()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                item.setFingers(progress + 1);
                seekTextView.setText(Integer.toString(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        v.findViewById(R.id.upButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFingerAction(MultiFingerGestureDetector.SWIPE_UP);
            }
        });

        v.findViewById(R.id.downButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFingerAction(MultiFingerGestureDetector.SWIPE_DOWN);
            }
        });

        v.findViewById(R.id.leftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFingerAction(MultiFingerGestureDetector.SWIPE_LEFT);
            }
        });

        v.findViewById(R.id.rightButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFingerAction(MultiFingerGestureDetector.SWIPE_RIGHT);
            }
        });

        v.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.removeLastTrace();
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

        v.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        v.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onEdited(getArguments().getInt(ARG_INDEX, -1), item);
                getFragmentManager().popBackStack();
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new MfsFingerAdapter(getActivity(), item.getTraces(), null);
        recyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    item.setAction(ActionActivity.getActionFromIntent(data));
                    String text = item.getAction().toString(nameArray);
                    actionButton.setText(text != null ? text : getText(R.string.action_empty));
                }
                break;
        }
    }

    private void addFingerAction(int action) {
        if (item.checkTrace(action)) {
            item.addTrace(action);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof OnMfsEditFragmentListener)
            listener = (OnMfsEditFragmentListener) getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && getActivity() instanceof OnMfsEditFragmentListener)
            listener = (OnMfsEditFragmentListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    interface OnMfsEditFragmentListener {
        void onEdited(int index, MultiFingerGestureItem item);
    }

    private static class MfsFingerAdapter extends ArrayRecyclerAdapter<Integer, MfsFingerAdapter.ViewHolder> {
        public MfsFingerAdapter(Context context, List<Integer> list, OnRecyclerListener listener) {
            super(context, list, listener);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(ViewHolder holder, Integer item, int position) {
            holder.title.setText((position + 1) + ".");
            holder.title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, getImage(item), 0);
            holder.icon.setImageResource(getImage(item));
        }

        private
        @DrawableRes
        int getImage(int type) {
            switch (type) {
                case MultiFingerGestureDetector.SWIPE_UP:
                    return R.drawable.ic_arrow_upward_white_24dp;
                case MultiFingerGestureDetector.SWIPE_DOWN:
                    return R.drawable.ic_arrow_downward_white_24dp;
                case MultiFingerGestureDetector.SWIPE_LEFT:
                    return R.drawable.ic_arrow_back_white_24dp;
                case MultiFingerGestureDetector.SWIPE_RIGHT:
                    return R.drawable.ic_arrow_forward_white_24dp;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.fragment_multi_finger_edit_item, parent, false));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView title;
            ImageView icon;

            ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.numTextView);
                icon = (ImageView) itemView.findViewById(R.id.imageView);
            }
        }
    }
}
