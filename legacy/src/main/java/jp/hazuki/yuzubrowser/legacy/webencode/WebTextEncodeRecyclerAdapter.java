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

package jp.hazuki.yuzubrowser.legacy.webencode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener;
import jp.hazuki.yuzubrowser.ui.widget.recycler.SimpleViewHolder;

public class WebTextEncodeRecyclerAdapter extends ArrayRecyclerAdapter<WebTextEncode, SimpleViewHolder<WebTextEncode>> {

    private ArrayList<WebTextEncode> mData;

    public WebTextEncodeRecyclerAdapter(Context context, ArrayList<WebTextEncode> list, OnRecyclerListener listener) {
        super(context, list, listener);
        mData = list;
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, WebTextEncode encode, int position) {
        holder.getTextView().setText(encode.getEncoding());
    }

    @Override
    protected SimpleViewHolder<WebTextEncode> onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new SimpleViewHolder<>(inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false),
                android.R.id.text1, this);
    }

    public WebTextEncode get(int pos) {
        return mData.get(pos);
    }
}
