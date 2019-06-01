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
import jp.hazuki.yuzubrowser.download.core.data.*
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.download
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class DownloadDialog : DaggerAppCompatDialogFragment() {

    private lateinit var root: DocumentFile
    private lateinit var folderButton: Button
    private lateinit var filenameEditText: EditText

    private var meta: MetaData? = null
    private var file: DownloadFile? = null

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.dialog_download, null)

        filenameEditText = view.findViewById(R.id.filenameEditText)
        folderButton = view.findViewById(R.id.folderButton)

        val request = arguments.getParcelable<DownloadDialogRequest>(ARG_REQUEST)!!
        ui {
            val name = withContext(Dispatchers.Default) {
                val name: String
                if (request.resolver != null) {
                    val resolver = request.resolver
                    name = resolver.resolveName(Uri.parse(AppPrefs.download_folder.get()).toDocumentFile(activity))

                    if (resolver.mimeType.isNullOrEmpty()) {
                        val newType = getMimeType(name)
                        if (newType != MIME_TYPE_UNKNOWN) {
                            meta = MetaData(name, newType, resolver.contentLength, false)
                        }
                    }
                    file = DownloadFile(request.url, name, request.request)
                } else {
                    val metadata = getMetaData(activity, root, request)
                    name = metadata.name
                    meta = metadata
                    file = DownloadFile(request.url, metadata.name, request.request)
                }
                return@withContext name
            }

            filenameEditText.setText(name)
            view.findViewById<View>(R.id.editor).visibility = View.VISIBLE
            view.findViewById<View>(R.id.loading).visibility = View.GONE
        }

        root = Uri.parse(AppPrefs.download_folder.get()).toDocumentFile(activity)

        folderButton.text = if (root.name.isNullOrBlank())
            getText(R.string.pref_download_folder) else root.name

        folderButton.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_FOLDER)
        }

        return AlertDialog.Builder(activity)
            .setTitle(R.string.download)
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onResume() {
        super.onResume()
        val d = dialog as AlertDialog?
        d?.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener {
            val file = file ?: return@setOnClickListener
            val context = activity ?: return@setOnClickListener
            val input = filenameEditText.text.toString()

            if (input.isEmpty()) return@setOnClickListener

            val newFileName = if (meta?.name == input || file.name == input) {
                input
            } else {
                createUniqueFileName(root, input, TMP_FILE_SUFFIX)
            }

            file.name = newFileName
            context.download(root.uri, file, meta)
            d.dismiss()
        }
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

    private fun getMetaData(context: Context, root: DocumentFile, request: DownloadDialogRequest): MetaData {
        return MetaData(context, okHttpClient, root, request.url, request.request)
    }

    companion object {
        private const val ARG_REQUEST = "request"

        private const val REQUEST_FOLDER = 1

        internal operator fun invoke(request: DownloadDialogRequest): DownloadDialog {
            return DownloadDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_REQUEST, request)
                }
            }
        }

        operator fun invoke(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long, referrer: String?): DownloadDialog {
            return invoke(DownloadDialogRequest(
                url,
                DownloadRequest(referrer, userAgent, null),
                NameResolver(url, contentDisposition, mimeType, contentLength)
            ))
        }

        operator fun invoke(url: String, userAgent: String?, referrer: String? = null, defaultExt: String? = null): DownloadDialog {
            return invoke(DownloadDialogRequest(url, DownloadRequest(referrer, userAgent, defaultExt), null))
        }
    }
}
