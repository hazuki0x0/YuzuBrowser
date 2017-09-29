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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;

public class AdBlockMainFragment extends Fragment {

    private OnAdBlockMainListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ad_blcok_main, container, false);
        getActivity().setTitle(R.string.pref_ad_block);

        Switch sw = v.findViewById(R.id.adBlockSwitch);
        final View black = v.findViewById(R.id.blackListButton);
        final View white = v.findViewById(R.id.whiteListButton);
        final View whitePage = v.findViewById(R.id.whitePageListButton);

        boolean enable = AppData.ad_block.get();
        sw.setChecked(enable);
        black.setEnabled(enable);
        white.setEnabled(enable);
        whitePage.setEnabled(enable);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                black.setEnabled(isChecked);
                white.setEnabled(isChecked);
                whitePage.setEnabled(isChecked);
                AppData.ad_block.set(isChecked);
                AppData.commit(getActivity(), AppData.ad_block);
            }
        });

        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.openBlackList();
            }
        });

        white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.openWhiteList();
            }
        });

        whitePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.openWhitePageList();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnAdBlockMainListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnAdBlockMainListener {
        void openBlackList();

        void openWhiteList();

        void openWhitePageList();
    }
}
