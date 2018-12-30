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

package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.search.settings.SearchSimpleIconView
import jp.hazuki.yuzubrowser.legacy.search.settings.SearchUrl
import jp.hazuki.yuzubrowser.legacy.search.settings.SearchUrlManager
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.OnRecyclerListener
import jp.hazuki.yuzubrowser.ui.preference.YuzuPreferenceDialog

class SearchUrlPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    init {
        setNegativeButtonText(android.R.string.cancel)
    }

    class PreferenceDialog : YuzuPreferenceDialog(), OnRecyclerListener {

        private lateinit var manager: SearchUrlManager

        companion object {
            @JvmStatic
            fun newInstance(preference: Preference): YuzuPreferenceDialog {
                return YuzuPreferenceDialog.newInstance(PreferenceDialog(), preference)
            }
        }

        override fun onCreateDialogView(context: Context?): View {
            val activity = activity ?: throw IllegalStateException()

            manager = SearchUrlManager(context!!)
            val view = View.inflate(context, R.layout.recycler_view, null)
            val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            recyclerView.addItemDecoration(DividerItemDecoration(activity))

            recyclerView.adapter = Adapter(activity, manager, this)

            return view
        }

        override fun onRecyclerItemClicked(v: View, position: Int) {
            manager.selectedId = manager[position].id
            manager.save()
            AppData.search_url.set(manager[position].url)
            AppData.commit(context, AppData.search_url)
            dismiss()
        }

        override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean = false

        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
            builder.setPositiveButton(null, null)
        }

        override fun onDialogClosed(positiveResult: Boolean) = Unit

        private class Adapter(context: Context, val list: SearchUrlManager, listener: OnRecyclerListener) : ArrayRecyclerAdapter<SearchUrl, Adapter.UrlHolder>(context, list, listener) {
            val backgroundRes: Int

            init {
                val value = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
                backgroundRes = value.resourceId
            }

            override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): UrlHolder {
                return UrlHolder(inflater.inflate(R.layout.serach_url_spinner, parent, false), this)
            }

            override fun onBindViewHolder(holder: UrlHolder, item: SearchUrl, position: Int) {
                if (list.selectedId == item.id) {
                    holder.itemView.setBackgroundResource(R.color.selected_overlay)
                } else {
                    holder.itemView.setBackgroundResource(backgroundRes)
                }
            }

            class UrlHolder(view: View, adapter: Adapter) : ArrayRecyclerAdapter.ArrayViewHolder<SearchUrl>(view, adapter) {
                val icon = view.findViewById<SearchSimpleIconView>(R.id.iconColorView)!!
                val textView = view.findViewById<TextView>(R.id.titleTextView)!!

                override fun setUp(item: SearchUrl) {
                    super.setUp(item)
                    icon.setSearchUrl(item)
                    textView.text = item.title
                }
            }
        }
    }
}
