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

package jp.hazuki.yuzubrowser.legacy.utils.view.filelist;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils;
import jp.hazuki.yuzubrowser.core.utility.utils.Predicate;
import jp.hazuki.yuzubrowser.legacy.R;

public class FileListViewController {
    private final Context mContext;
    private File mCurrentFile;
    private File mCurrentFiles[];
    private ListView mListView;
    private OnFileSelectedListener mListener;
    private boolean mShowParentMover = false;
    private boolean mShowDirectoryOnly = false;
    private String extension = null;

    public FileListViewController(Context context) {
        mContext = context;
    }

    public FileListViewController(Context context, File file) {
        mContext = context;
        setFilePath(file);
    }

    public void setListView(ListView listView) {
        mListView = listView;
        if (listView != null && mCurrentFile != null)
            setFilePath(mCurrentFile);
    }

    public ListView getListView() {
        return mListView;
    }

    public interface OnFileSelectedListener {
        void onFileSelected(File file);

        boolean onDirectorySelected(File file);
    }

    public void setOnFileSelectedListener(OnFileSelectedListener l) {
        mListener = l;
    }

    public void setShowParentMover(boolean b) {
        mShowParentMover = b;
        notifyDataSetChanged();
    }

    public boolean isShowParentMover() {
        return mShowParentMover;
    }

    public void setShowDirectoryOnly(boolean b) {
        mShowDirectoryOnly = b;
        notifyDataSetChanged();
    }

    public void setShowExtensionOnly(String ext) {
        if (TextUtils.isEmpty(ext)) {
            extension = null;
        } else {
            extension = ext;
        }
    }

    public boolean isShowDirectoryOnly() {
        return mShowDirectoryOnly;
    }

    public boolean setFilePath(File file) {
        if (file == null) {
            mCurrentFile = null;
            mCurrentFiles = null;
            if (mListView != null) {
                ArrayAdapter<?> adapter = ((ArrayAdapter<?>) mListView.getAdapter());
                if (adapter != null)
                    adapter.notifyDataSetInvalidated();
            }
            return false;
        }
        if (file.isFile()) {
            return false;
        }
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(mContext, R.string.cannot_access_folder, Toast.LENGTH_SHORT).show();
            return false;
        }

        File[] file_list = file.listFiles();

        if (file_list == null) {
            Toast.makeText(mContext, R.string.cannot_access_folder, Toast.LENGTH_SHORT).show();
            return false;
        }

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

        ArrayAdapter<File> adapter = new ArrayAdapter<File>(mContext, android.R.layout.simple_list_item_1, file_list) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                }
                File file = getItem(position);
                TextView text1 = convertView.findViewById(android.R.id.text1);
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
        };
        if (mListView != null)
            mListView.setAdapter(adapter);

        return true;
    }

    public boolean goBack() {
        File nextFile = mCurrentFile.getParentFile();
        if (nextFile != null && nextFile.canRead())
            return setFilePath(nextFile);
        else
            return false;
    }

    public void notifySelectThisFolder() {
        if (mListener != null)
            mListener.onFileSelected(mCurrentFile);
    }

    public boolean onItemClick(int position) {
        if (mShowParentMover) {
            if (position == 0) {
                goBack();
                return false;
            }
            --position;
        }
        File file = mCurrentFiles[position];
        if (file.isFile()) {
            if (mListener != null)
                mListener.onFileSelected(file);
            return true;
        } else {
            setFilePath(file);
            return false;
        }
    }

    public void notifyDataSetChanged() {
        if (mCurrentFile != null)
            setFilePath(mCurrentFile);
    }

    protected File getCurrentFolder() {
        return mCurrentFile;
    }

    protected File[] getCurrentFileList() {
        return mCurrentFiles;
    }
}
