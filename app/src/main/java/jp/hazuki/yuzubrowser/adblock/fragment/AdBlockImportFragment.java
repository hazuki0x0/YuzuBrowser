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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.adblock.AdBlock;
import jp.hazuki.yuzubrowser.adblock.AdBlockDecoder;
import jp.hazuki.yuzubrowser.utils.IOUtils;

public class AdBlockImportFragment extends Fragment {
    private static final String ARG_URI = "uri";

    private OnImportListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ad_block_import, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final EditText editText = view.findViewById(R.id.editText);
        final CheckBox checkBox = view.findViewById(R.id.excludeCheckBox);

        Uri uri = getArguments().getParcelable(ARG_URI);

        if (uri == null)
            throw new IllegalArgumentException();

        try (InputStream is = getActivity().getContentResolver().openInputStream(uri)) {
            editText.setText(IOUtils.readString(is));
        } catch (IOException e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AdBlock> adBlocks = AdBlockDecoder.decode(editText.getText().toString(), checkBox.isChecked());
                listener.onImport(adBlocks);
                getFragmentManager().popBackStack();
            }
        });

        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnImportListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public static AdBlockImportFragment newInstance(Uri uri) {
        AdBlockImportFragment fragment = new AdBlockImportFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_URI, uri);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface OnImportListener {
        void onImport(List<AdBlock> adBlocks);
    }
}
