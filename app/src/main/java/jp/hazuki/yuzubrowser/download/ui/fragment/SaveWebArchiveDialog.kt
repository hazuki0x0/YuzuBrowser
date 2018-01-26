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
import android.widget.CheckBox
import android.widget.EditText
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.utils.guessDownloadFileName
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.download
import jp.hazuki.yuzubrowser.download.service.DownloadFile
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.createUniqueFileName
import jp.hazuki.yuzubrowser.webkit.CustomWebView

class SaveWebArchiveDialog : DialogFragment() {
    private var listener: OnSaveWebViewListener? = null

    private lateinit var root: DocumentFile
    private lateinit var folderButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.dialog_download, null)

        val filenameEditText = view.findViewById<EditText>(R.id.filenameEditText)
        val saveArchiveCheckBox = view.findViewById<CheckBox>(R.id.saveArchiveCheckBox)
        folderButton = view.findViewById(R.id.folderButton)

        saveArchiveCheckBox.visibility = View.VISIBLE

        val file = arguments.getParcelable<DownloadFile>(ARG_FILE)

        root = Uri.parse(AppData.download_folder.get()).toDocumentFile(activity)

        val name = file.name ?: "index$EXT_HTML"
        filenameEditText.setText(name)

        folderButton.text = root.name
        folderButton.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_FOLDER)
        }

        saveArchiveCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filenameEditText.setText(guessDownloadFileName(root, file.url, null, MIME_TYPE_MHTML, EXT_MHTML))
            } else {
                filenameEditText.setText(guessDownloadFileName(root, file.url, null, MIME_TYPE_HTML, EXT_HTML))
            }
        }

        return AlertDialog.Builder(activity)
                .setTitle(R.string.download)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val act = getActivity() ?: return@setPositiveButton
                    val input = filenameEditText.text.toString()
                    if (input.isEmpty()) return@setPositiveButton

                    val newFileName = createUniqueFileName(root, input, Constants.download.TMP_FILE_SUFFIX)

                    file.name = newFileName

                    if (saveArchiveCheckBox.isChecked) {
                        listener?.onSaveWebViewToFile(root, file, arguments.getInt(ARG_NO))
                    } else {
                        act.download(root.uri, file, null)
                    }

                    this.dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as? OnSaveWebViewListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val MIME_TYPE_MHTML = Constants.mimeType.MHTML
        private const val MIME_TYPE_HTML = Constants.mimeType.HTML
        private const val EXT_MHTML = ".mhtml"
        private const val EXT_HTML = ".html"

        private const val ARG_FILE = "file"
        private const val ARG_NO = "no"

        private const val REQUEST_FOLDER = 1

        operator fun invoke(file: DownloadFile): SaveWebArchiveDialog {
            return SaveWebArchiveDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FILE, file)
                }
            }
        }


        operator fun invoke(context: Context, url: String, webView: CustomWebView, webViewNo: Int): SaveWebArchiveDialog {
            val name = guessDownloadFileName(Uri.parse(AppData.download_folder.get()).toDocumentFile(context),
                    url, null, MIME_TYPE_HTML, null)

            return SaveWebArchiveDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FILE, DownloadFile(url, name,
                            DownloadRequest(null, webView.settings.userAgentString, EXT_HTML)))
                    putInt(ARG_NO, webViewNo)
                }
            }
        }
    }

    interface OnSaveWebViewListener {
        fun onSaveWebViewToFile(root: DocumentFile, file: DownloadFile, webViewNo: Int)
    }
}