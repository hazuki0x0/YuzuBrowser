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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.gesture.multiFinger.data.MultiFingerGestureItem;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

public class MfsListAdapter extends ArrayRecyclerAdapter<MultiFingerGestureItem, MfsListAdapter.ViewHolder> {

    private ActionNameArray nameList;

    public MfsListAdapter(Context context, List<MultiFingerGestureItem> list, ActionNameArray nameArray, OnRecyclerListener listener) {
        super(context, list, listener);

        nameList = nameArray;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, MultiFingerGestureItem item, int position) {
        Action action = item.getAction();

        if (action == null || action.isEmpty())
            holder.title.setText(R.string.action_empty);
        else
            holder.title.setText(action.toString(nameList));
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
