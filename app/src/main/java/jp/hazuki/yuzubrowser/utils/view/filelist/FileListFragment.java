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

package jp.hazuki.yuzubrowser.utils.view.filelist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.util.Predicate;

public class FileListFragment extends ListFragment {
    private static final String EXTRA_FILE = "file";
    private static final String EXTRA_PARENT_MOVER = "mover";
    private static final String EXTRA_DIR_ONLY = "dir_only";

    private File mCurrentFile;
    private File mCurrentFiles[];
    private boolean mShowParentMover = false;
    private boolean mShowDirectoryOnly = false;
    private String extension = null;
    private OnFileSelectedListener fileSelectedListener;
    private OnFileItemLongClickListener itemLongClick;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mShowParentMover = getArguments().getBoolean(EXTRA_PARENT_MOVER);
        mShowDirectoryOnly = getArguments().getBoolean(EXTRA_DIR_ONLY);
        File file = (File) getArguments().getSerializable(EXTRA_FILE);

        setFilePath(file);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (itemLongClick != null) {
                    itemLongClick.onListFileItemLongClick(getListView(), view, mCurrentFiles[position], position, id);
                }
                return false;
            }
        });
    }

    public boolean goBack() {
        File nextFile = mCurrentFile.getParentFile();
        if (nextFile != null && nextFile.canRead())
            return setFilePath(nextFile);
        else
            return false;
    }

    public boolean setFilePath(File file) {
        if (file == null) {
            mCurrentFile = null;
            mCurrentFiles = null;
            if (getListView() != null) {
                ArrayAdapter<?> adapter = ((ArrayAdapter<?>) getListView().getAdapter());
                if (adapter != null)
                    adapter.notifyDataSetInvalidated();
            }
            return false;
        }
        if (file.isFile()) {
            return false;
        }

        if (!file.exists() || !file.canRead()) {
            Toast.makeText(getActivity(), R.string.cannot_access_folder, Toast.LENGTH_SHORT).show();
            return false;
        }

        File[] file_list = file.listFiles();
        if (mShowDirectoryOnly) {
            file_list = ArrayUtils.copyIf(file_list, new Predicate<File>() {
                @Override
                public boolean evaluate(File object) {
                    return object.isDirectory();
                }
            });
        }

        if (extension != null) {
            file_list = ArrayUtils.copyIf(file_list, new Predicate<File>() {
                @Override
                public boolean evaluate(File object) {
                    return object.isDirectory() || object.getName().endsWith(extension);
                }
            });
        }

        Arrays.sort(file_list, FileUtils.FILE_COMPARATOR);

        mCurrentFile = file;
        mCurrentFiles = file_list;

        FileAdapter adapter = new FileAdapter(getActivity(), file_list);
        setListAdapter(adapter);
        getActivity().setTitle(file.getName());
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mShowParentMover) {
            if (position == 0) {
                goBack();
                return;
            }
            --position;
        }
        File file = mCurrentFiles[position];
        if (file.isFile()) {
            if (fileSelectedListener != null) {
                fileSelectedListener.onFileSelected(file);
            }
        } else {
            setFilePath(file);
        }
    }

    public void notifyDataSetChanged() {
        if (mCurrentFile != null)
            setFilePath(mCurrentFile);
    }

    public File getCurrentFolder() {
        return mCurrentFile;
    }

    public File[] getCurrentFileList() {
        return mCurrentFiles;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof OnFileSelectedListener) {
            fileSelectedListener = (OnFileSelectedListener) getActivity();
        } else {
            throw new RuntimeException("Not found OnFileSelectedListener in Activity");
        }

        if (getActivity() instanceof OnFileItemLongClickListener) {
            itemLongClick = (OnFileItemLongClickListener) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fileSelectedListener = null;
        itemLongClick = null;
    }

    public static FileListFragment newInstance(File file, boolean parentMover, boolean directoryOnly) {
        FileListFragment fragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_FILE, file);
        bundle.putBoolean(EXTRA_PARENT_MOVER, parentMover);
        bundle.putBoolean(EXTRA_DIR_ONLY, directoryOnly);
        fragment.setArguments(bundle);
        return fragment;
    }

    private class FileAdapter extends ArrayAdapter<File> {

        private FileAdapter(Context context, File[] files) {
            super(context, android.R.layout.simple_expandable_list_item_1, files);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            File file = getItem(position);
            TextView text1 = ((TextView) convertView.findViewById(android.R.id.text1));
            text1.setText((file == null) ? "../" : (file.isDirectory() ? file.getName() + File.separatorChar : file.getName()));
            return convertView;
        }

        @Override
        public File getItem(int position) {
            if (mShowParentMover)
                if (position == 0)
                    return null;
                else
                    return super.getItem(position - 1);
            else
                return super.getItem(position);
        }

        @Override
        public int getCount() {
            if (mShowParentMover)
                return super.getCount() + 1;
            else
                return super.getCount();
        }
    }

    public interface OnFileItemLongClickListener {
        boolean onListFileItemLongClick(ListView l, View v, File file, int position, long id);
    }

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }
}
