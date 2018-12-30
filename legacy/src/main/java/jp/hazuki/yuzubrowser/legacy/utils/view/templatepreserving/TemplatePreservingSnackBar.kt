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

package jp.hazuki.yuzubrowser.legacy.utils.view.templatepreserving

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import jp.hazuki.yuzubrowser.legacy.R

class TemplatePreservingSnackBar
/**
 * Constructor for the transient bottom bar.
 *
 * @param parent              The parent for this transient bottom bar.
 * @param content             The content view for this transient bottom bar.
 * @param contentViewCallback The content view callback for this transient bottom bar.
 */
private constructor(parent: ViewGroup, content: View, contentViewCallback: ContentViewCallback) : BaseTransientBottomBar<TemplatePreservingSnackBar>(parent, content, contentViewCallback) {

    private val textView: TemplatePreservingTextView = content.findViewById(R.id.snackbarText)
    private val action: TextView = content.findViewById(R.id.snackbarAction)

    var isDismissByAction = false
        private set

    fun setTemplateText(text: String) {
        textView.setTemplateText(text)
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }

    fun setAction(@StringRes text: Int, listener: View.OnClickListener): TemplatePreservingSnackBar {
        return setAction(context.getText(text), listener)
    }

    fun setAction(text: CharSequence, listener: View.OnClickListener?): TemplatePreservingSnackBar {
        val tv = action

        if (TextUtils.isEmpty(text) || listener == null) {
            tv.visibility = View.GONE
            tv.setOnClickListener(null)
        } else {
            tv.visibility = View.VISIBLE
            tv.text = text
            tv.setOnClickListener {
                listener.onClick(it)
                // Now dismiss the Snackbar
                isDismissByAction = true
                dismiss()
            }
        }
        return this
    }

    private class ContentViewCallback internal constructor(private val content: View) : com.google.android.material.snackbar.ContentViewCallback {

        override fun animateContentIn(delay: Int, duration: Int) {
            // add custom *in animations for your views
            // e.g. original snackbar uses alpha animation, from 0 to 1
            content.scaleY = 0f
            content.animate()
                    .scaleY(1f)
                    .setDuration(duration.toLong()).startDelay = delay.toLong()
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            // add custom *out animations for your views
            // e.g. original snackbar uses alpha animation, from 1 to 0
            content.scaleY = 1f
            content.animate()
                    .scaleY(0f)
                    .setDuration(duration.toLong()).startDelay = delay.toLong()
        }
    }

    companion object {

        fun make(view: ViewGroup, template: String, title: CharSequence, duration: Int): TemplatePreservingSnackBar {
            val parent = findSuitableParent(view) ?: throw IllegalArgumentException("No suitable parent found from the given view. " + "Please provide a valid view.")
            val inflater = LayoutInflater.from(parent.context)
            val content = inflater.inflate(R.layout.template_preserving_snackbar, parent, false)

            val callback = ContentViewCallback(content)
            val snackBar = TemplatePreservingSnackBar(parent, content, callback)
            snackBar.duration = duration
            snackBar.setTemplateText(template)
            snackBar.setText(title)
            return snackBar
        }

        private fun findSuitableParent(view: View?): ViewGroup? {
            var selection = view
            var fallback: ViewGroup? = null
            do {
                if (selection is androidx.coordinatorlayout.widget.CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return selection
                } else if (selection is FrameLayout) {
                    if (selection.id == android.R.id.content) {
                        // If we've hit the decor content view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return selection
                    } else {
                        // It's not the content view but we'll use it as our fallback
                        fallback = selection
                    }
                }

                if (selection != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = selection.parent
                    selection = if (parent is View) parent else null
                }
            } while (selection != null)

            // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
            return fallback
        }
    }
}
