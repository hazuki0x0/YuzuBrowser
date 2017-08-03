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

package jp.hazuki.yuzubrowser.speeddial.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

class SpeedDialRecyclerAdapter extends ArrayRecyclerAdapter<SpeedDial, SpeedDialRecyclerAdapter.ViewHolder> {

    private ArrayList<SpeedDial> mData;

    public SpeedDialRecyclerAdapter(Context context, ArrayList<SpeedDial> list, OnRecyclerListener listener) {
        super(context, list, listener);
        mData = list;
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_2, parent, false), this);
    }

    public SpeedDial get(int pos) {
        return mData.get(pos);
    }

    class ViewHolder extends ArrayRecyclerAdapter.ArrayViewHolder<SpeedDial> {

        TextView title;
        TextView url;

        public ViewHolder(View itemView, SpeedDialRecyclerAdapter adapter) {
            super(itemView, adapter);
            title = (TextView) itemView.findViewById(android.R.id.text1);
            url = (TextView) itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void setUp(SpeedDial item) {
            super.setUp(item);
            title.setText(item.getTitle());
            url.setText(item.getUrl());
        }
    }
}
