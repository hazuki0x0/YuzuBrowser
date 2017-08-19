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

package jp.hazuki.yuzubrowser.reader;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.UrlUtils;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragmentCompat;

public class ReaderFragment extends Fragment implements LoaderManager.LoaderCallbacks<ReaderData> {
    private static final String ARG_URL = "url";
    private static final String ARG_UA = "ua";

    private ProgressDialog progressDialog;
    private TextView titleTextView;
    private TextView bodyTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reader, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        titleTextView = view.findViewById(R.id.titleTextView);
        bodyTextView = view.findViewById(R.id.bodyTextView);

        bodyTextView.setTextSize(AppData.reader_text_size.get());

        String fontPath = AppData.reader_text_font.get();
        if (!TextUtils.isEmpty(fontPath)) {
            File font = new File(fontPath);

            if (font.exists() && font.isFile()) {
                try {
                    bodyTextView.setTypeface(Typeface.createFromFile(fontPath));
                } catch (RuntimeException e) {
                    Toast.makeText(getActivity(), R.string.font_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.font_not_found, Toast.LENGTH_SHORT).show();
            }
        }

        String url = getArguments().getString(ARG_URL);
        if (TextUtils.isEmpty(url)) {
            setFailedText();
            return;
        }

        getActivity().setTitle(UrlUtils.decodeUrlHost(url));

        getLoaderManager().initLoader(0, getArguments(), this);
    }

    public static ReaderFragment newInstance(String url, String userAgent) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_URL, url);
        bundle.putString(ARG_UA, userAgent);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void setFailedText() {
        setText(getString(R.string.untitled), getString(R.string.loading_failed));
    }

    private void setText(String title, String body) {
        titleTextView.setText(title);
        bodyTextView.setText(body);
    }

    @Override
    public Loader<ReaderData> onCreateLoader(int id, Bundle args) {
        progressDialog = ProgressDialog.newInstance(getString(R.string.now_loading));
        progressDialog.show(getChildFragmentManager(), "loading");
        return new ReaderTask(getContext(), args.getString(ARG_URL), args.getString(ARG_UA));
    }

    @Override
    public void onLoadFinished(Loader<ReaderData> loader, ReaderData data) {
        new Handler().post(() -> progressDialog.dismiss());
        if (data != null) {
            setText(data.getTitle(), data.getBody());
        } else {
            setFailedText();
        }
    }

    @Override
    public void onLoaderReset(Loader<ReaderData> loader) {

    }

    public static class ProgressDialog extends ProgressDialogFragmentCompat {

        private static final String MESSAGE = "mes";

        public static ProgressDialog newInstance(String message) {
            ProgressDialog fragment = new ProgressDialog();
            Bundle bundle = new Bundle();
            bundle.putCharSequence(MESSAGE, message);
            fragment.setArguments(bundle);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
        }
    }
}
