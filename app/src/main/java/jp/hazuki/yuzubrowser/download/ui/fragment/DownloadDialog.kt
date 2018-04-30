/*
 * Copyright (C) 2017-2018 Hazuki
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
import android.support.v4.app.DialogFragment
import android.support.v4.provider.DocumentFile
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.core.utils.guessDownloadFileName
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.download
import jp.hazuki.yuzubrowser.download.service.DownloadFile
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.async
import jp.hazuki.yuzubrowser.utils.createUniqueFileName
import jp.hazuki.yuzubrowser.utils.ui

class DownloadDialog : DialogFragment() {

    private lateinit var root: DocumentFile
    private lateinit var folderButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.dialog_download, null)
        view.visibility = View.GONE

        val filenameEditText = view.findViewById<EditText>(R.id.filenameEditText)
        folderButton = view.findViewById(R.id.folderButton)

        val file = arguments.getParcelable<DownloadFile>(ARG_FILE)
        var meta = arguments.getParcelable<MetaData?>(ARG_META)

        root = Uri.parse(AppData.download_folder.get()).toDocumentFile(activity)

        val name = file.name
        if (name != null) {
            val resolvedName = createUniqueFileName(root, name)
            filenameEditText.setText(resolvedName)
            view.visibility = View.VISIBLE
        } else {
            ui {
                val metadata = getMetaData(activity, root, file).await()
                filenameEditText.setText(metadata.name)
                view.visibility = View.VISIBLE
                meta = metadata
            }
        }

        folderButton.text = root.name
        folderButton.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_FOLDER)
        }

        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.download)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val context = getActivity() ?: return@setPositiveButton
                    val input = filenameEditText.text.toString()
                    if (input.isEmpty()) return@setPositiveButton

                    val newFileName = createUniqueFileName(root, input, Constants.download.TMP_FILE_SUFFIX)

                    file.name = newFileName
                    context.download(root.uri, file, meta)
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val context = getActivity() ?: return@setOnClickListener
                val input = filenameEditText.text.toString()
                if (input.isEmpty()) return@setOnClickListener

                val newFileName = createUniqueFileName(root, input, Constants.download.TMP_FILE_SUFFIX)

                file.name = newFileName
                context.download(root.uri, file, meta)
                dialog.dismiss()
            }
        }
        return dialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activity = activity ?: return

        when (requestCode) {
            REQUEST_FOLDER -> {
                if (resultCode != Activity.RESULT_OK || data == null) return

                root = data.data.toDocumentFile(activity)
                folderButton.text = root.name
            }
        }
    }

    private fun getMetaData(context: Context, root: DocumentFile, file: DownloadFile) = async {
        return@async MetaData(context, root, file.url, file.request)
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
            val name = guessDownloadFileName(Uri.parse(AppData.download_folder.get()).toDocumentFile(context),
                    url, contentDisposition, mimeType, null)

            return invoke(DownloadFile(url, name, DownloadRequest(referrer, userAgent, null)),
                    if (mimeType != null) MetaData(name, mimeType, contentLength, false) else null)
        }

        operator fun invoke(url: String, userAgent: String?, referrer: String? = null, defaultExt: String? = null): DownloadDialog {
            return invoke(DownloadFile(url, null, DownloadRequest(referrer, userAgent, defaultExt)))
        }
    }
}