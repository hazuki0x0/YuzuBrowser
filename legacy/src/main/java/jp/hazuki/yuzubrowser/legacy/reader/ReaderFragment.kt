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

package jp.hazuki.yuzubrowser.legacy.reader

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.core.utility.extensions.isInstanceOf
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.dialog.ProgressDialog
import jp.hazuki.yuzubrowser.ui.extensions.decodePunyCodeUrlHost
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

class ReaderFragment : DaggerFragment() {

    private lateinit var titleTextView: TextView
    private lateinit var bodyTextView: TextView

    @Inject
    internal lateinit var okHttp: OkHttpClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return
        val arguments = arguments ?: throw IllegalArgumentException()

        titleTextView = view.findViewById(R.id.titleTextView)
        bodyTextView = view.findViewById(R.id.bodyTextView)

        bodyTextView.textSize = AppPrefs.reader_text_size.get().toFloat()

        val fontPath = AppPrefs.reader_text_font.get()
        if (!TextUtils.isEmpty(fontPath)) {
            val font = File(fontPath)

            if (font.exists() && font.isFile) {
                try {
                    bodyTextView.typeface = Typeface.createFromFile(fontPath)
                } catch (e: RuntimeException) {
                    Toast.makeText(activity, R.string.font_error, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, R.string.font_not_found, Toast.LENGTH_SHORT).show()
            }
        }

        bodyTextView.viewTreeObserver.addOnGlobalLayoutListener {
            val sequence = bodyTextView.text
            if (sequence is Spanned) {
                setImages(sequence)
                bodyTextView.text = sequence
            }
        }

        val url = arguments.getString(ARG_URL)
        if (url.isNullOrBlank()) {
            setFailedText()
            return
        }

        activity.title = url.decodePunyCodeUrlHost()

        fragmentManager?.findFragmentByTag("loading").isInstanceOf<androidx.fragment.app.DialogFragment> {
            it.dismiss()
        }
        val dialog = ProgressDialog(activity.getString(R.string.now_loading), true, false).also {
            it.show(childFragmentManager, "loading")
        }

        ui {
            val data = withContext(Dispatchers.Default) {
                okHttp.decodeToReaderData(activity.applicationContext, url, arguments.getString(ARG_UA))
            }
            if (isAdded) {
                if (isResumed) {
                    dialog.dismiss()
                }
                if (data != null) {
                    setText(data.title, data.body)
                } else {
                    setFailedText()
                }
            }
        }
    }

    private fun setFailedText() {
        setText(getString(R.string.untitled), getString(R.string.loading_failed))
    }

    private fun setText(title: String, body: CharSequence) {
        titleTextView.text = title

        if (body is Spanned) {
            setImages(body)
        }

        bodyTextView.text = body
    }

    private fun setImages(spanned: Spanned) {
        val activity = activity ?: return

        val imageSpans = spanned.getSpans(0, spanned.length, ImageSpan::class.java)

        var width = bodyTextView.width

        if (width == 0) {
            return
        }

        val maxWidth = activity.convertDpToPx(360)
        var padding = 0

        if (width > maxWidth) {
            padding = (width - maxWidth) / 2
            width = maxWidth
        }
        for (span in imageSpans) {
            val drawable = span.drawable

            val w = drawable.intrinsicWidth
            val h = drawable.intrinsicHeight

            if (w <= 0 || h <= 0) {
                drawable.setBounds(0, 0, 0, 0)
                return
            }

            val scale = width / w.toDouble()
            drawable.setBounds(padding, 0, (w * scale).toInt() + padding, (h * scale).toInt())
        }
    }

    companion object {
        private const val ARG_URL = "url"
        private const val ARG_UA = "ua"

        operator fun invoke(url: String?, userAgent: String?): ReaderFragment {
            return ReaderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putString(ARG_UA, userAgent)
                }
            }
        }
    }
}
