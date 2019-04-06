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

package jp.hazuki.yuzubrowser.download.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import dagger.android.support.DaggerAppCompatDialogFragment
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
import jp.hazuki.yuzubrowser.core.utility.utils.createUniqueFileName
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.download.R
import jp.hazuki.yuzubrowser.download.TMP_FILE_SUFFIX
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.core.utils.guessDownloadFileName
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.download
import jp.hazuki.yuzubrowser.download.settings.DownloadPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class DownloadDialog : DaggerAppCompatDialogFragment() {

    private lateinit var root: DocumentFile
    private lateinit var folderButton: Button

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.dialog_download, null)
        view.visibility = View.GONE

        val filenameEditText = view.findViewById<EditText>(R.id.filenameEditText)
        folderButton = view.findViewById(R.id.folderButton)

        val file = arguments.getParcelable<DownloadFile>(ARG_FILE)
        var meta = arguments.getParcelable<MetaData?>(ARG_META)

        checkNotNull(file)

        root = Uri.parse(DownloadPrefs.get(activity).downloadFolder).toDocumentFile(activity)

        val name = file.name
        if (name != null) {
            filenameEditText.setText(name)
            view.visibility = View.VISIBLE
        } else {
            ui {
                val metadata = withContext(Dispatchers.Default) { getMetaData(activity, root, file) }
                filenameEditText.setText(metadata.name)
                view.visibility = View.VISIBLE
                meta = metadata
            }
        }

        folderButton.text = root.name
        folderButton.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_FOLDER)
        }

        return AlertDialog.Builder(activity)
                .setTitle(R.string.download)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val context = getActivity() ?: return@setPositiveButton
                    val input = filenameEditText.text.toString()
                    if (input.isEmpty()) return@setPositiveButton

                    val newFileName = if (meta?.name == input || file.name == input) {
                        input
                    } else {
                        createUniqueFileName(root, input, TMP_FILE_SUFFIX)
                    }

                    file.name = newFileName
                    context.download(root.uri, file, meta)
                    dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activity = activity ?: return

        when (requestCode) {
            REQUEST_FOLDER -> {
                if (resultCode != Activity.RESULT_OK || data == null) return
                val uri = data.data
                checkNotNull(uri) {"intent uri is null"}

                root = uri.toDocumentFile(activity)
                folderButton.text = root.name
            }
        }
    }

    private fun getMetaData(context: Context, root: DocumentFile, file: DownloadFile): MetaData {
        return MetaData(context, okHttpClient, root, file.url, file.request)
    }

    companion object {
        private const val ARG_FILE = "file"
        private const val ARG_META = "meta"

        private const val REQUEST_FOLDER = 1

        operator fun invoke(file: DownloadFile, metaData: MetaData? = null): DownloadDialog {
            return DownloadDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FILE, file)
                    if (metaData != null) {
                        putParcelable(ARG_META, metaData)
                    }
                }
            }
        }

        operator fun invoke(context: Context, url: String, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long, referrer: String?): DownloadDialog {
            val name = guessDownloadFileName(Uri.parse(DownloadPrefs.get(context).downloadFolder).toDocumentFile(context),
                    url, contentDisposition, mimeType, null)

            if (mimeType.isNullOrEmpty()) {
                val newType = getMimeType(name)
                if (newType != MIME_TYPE_UNKNOWN) {
                    return invoke(DownloadFile(url, name, DownloadRequest(referrer, userAgent, null)),
                            MetaData(name, newType, contentLength, false))
                }
                return invoke(DownloadFile(url, name, DownloadRequest(referrer, userAgent, null)), null)
            }

            return invoke(DownloadFile(url, name, DownloadRequest(referrer, userAgent, null)),
                    MetaData(name, mimeType, contentLength, false))
        }

        operator fun invoke(url: String, userAgent: String?, referrer: String? = null, defaultExt: String? = null): DownloadDialog {
            return invoke(DownloadFile(url, null, DownloadRequest(referrer, userAgent, defaultExt)))
        }
    }
}