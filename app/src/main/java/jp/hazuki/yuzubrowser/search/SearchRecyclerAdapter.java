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

package jp.hazuki.yuzubrowser.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.search.suggest.Suggestion;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;

public class SearchRecyclerAdapter extends ArrayRecyclerAdapter<Suggestion, SearchRecyclerAdapter.SuggestionViewHolder> {

    private final OnSuggestSelectListener listener;

    public SearchRecyclerAdapter(Context context, List<Suggestion> list, @NonNull OnSuggestSelectListener listener) {
        super(context, list, null);

        this.listener = listener;
    }

    @Override
    protected SuggestionViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new SuggestionViewHolder(inflater.inflate(R.layout.search_activity_list_item, parent, false), this);
    }


    public void onSelectSuggest(String query) {
        listener.onSelectSuggest(query);
    }

    public void onInputSuggest(String query) {
        listener.onInputSuggest(query);
    }

    public void onLongClicked(String query) {
        listener.onLongClicked(query);
    }

    public static class SuggestionViewHolder extends ArrayRecyclerAdapter.ArrayViewHolder<Suggestion> {

        private TextView text;

        public SuggestionViewHolder(View itemView, final SearchRecyclerAdapter adapter) {
            super(itemView, adapter);
            text = (TextView) itemView.findViewById(R.id.textView);

            itemView.findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onInputSuggest(getItem().word);
                }
            });

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onSelectSuggest(getItem().word);
                }
            });

            text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getItem().history)
                        adapter.onLongClicked(getItem().word);
                    return true;
                }
            });
        }

        @Override
        public void setUp(Suggestion item) {
            super.setUp(item);
            text.setText(item.word);
        }
    }

    public interface OnSuggestSelectListener {
        void onSelectSuggest(String query);

        void onInputSuggest(String query);

        void onLongClicked(String query);
    }
}
