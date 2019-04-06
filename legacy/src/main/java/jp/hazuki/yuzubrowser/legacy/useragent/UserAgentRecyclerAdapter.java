/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.useragent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener;

public class UserAgentRecyclerAdapter extends ArrayRecyclerAdapter<UserAgent, UserAgentRecyclerAdapter.ViewHolder> {

    public UserAgentRecyclerAdapter(Context context, ArrayList<UserAgent> list, OnRecyclerListener listener) {
        super(context, list, listener);
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_2
                , parent, false), this);
    }

    class ViewHolder extends ArrayRecyclerAdapter.ArrayViewHolder<UserAgent> {

        TextView title;
        TextView userAgent;

        public ViewHolder(View itemView, UserAgentRecyclerAdapter adapter) {
            super(itemView, adapter);
            title = itemView.findViewById(android.R.id.text1);
            userAgent = itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void setUp(UserAgent item) {
            super.setUp(item);
            title.setText(item.getName());
            userAgent.setText(item.getUseragent());
        }
    }
}
