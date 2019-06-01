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

package jp.hazuki.yuzubrowser.search.presentation.settings

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import dagger.android.support.AndroidSupportInjection
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.domain.ISearchUrlRepository
import jp.hazuki.yuzubrowser.search.model.provider.SearchSuggestProviders
import jp.hazuki.yuzubrowser.search.presentation.widget.SearchSimpleIconView
import jp.hazuki.yuzubrowser.ui.preference.YuzuPreferenceDialog
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import javax.inject.Inject

class SearchUrlPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    init {
        setNegativeButtonText(android.R.string.cancel)
    }

    class PreferenceDialog : YuzuPreferenceDialog(), OnRecyclerListener {

        @Inject
        lateinit var manager: ISearchUrlRepository
        @Inject
        lateinit var moshi: Moshi
        @Inject
        internal lateinit var faviconManager: FaviconManager

        private lateinit var provider: SearchSuggestProviders

        companion object {
            @JvmStatic
            fun newInstance(preference: Preference): YuzuPreferenceDialog {
                return newInstance(PreferenceDialog(), preference)
            }
        }

        override fun onCreateDialogView(context: Context?): View {
            val activity = activity ?: throw IllegalStateException()
            AndroidSupportInjection.inject(this)

            provider = SearchSuggestProviders(manager.load())

            val view = View.inflate(context, R.layout.recycler_view, null)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(activity)

            recyclerView.adapter = Adapter(activity, provider, faviconManager, this)

            return view
        }

        override fun onRecyclerItemClicked(v: View, position: Int) {
            provider.selectedId = provider[position].id
            manager.save(provider.toSettings())
            AppPrefs.search_url.set(provider[position].url)
            AppPrefs.commit(requireActivity(), AppPrefs.search_url)
            dismiss()
        }

        override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean = false

        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
            builder.setPositiveButton(null, null)
        }

        override fun onDialogClosed(positiveResult: Boolean) = Unit

        private class Adapter(
            context: Context,
            val providers: SearchSuggestProviders,
            val faviconManager: FaviconManager,
            private val listener: OnRecyclerListener
        ) : RecyclerView.Adapter<Adapter.UrlHolder>() {
            private val inflater = LayoutInflater.from(context)

            override fun onBindViewHolder(holder: UrlHolder, position: Int) {
                val item = providers[position]

                if (providers.selectedId == item.id) {
                    holder.itemView.setBackgroundResource(R.color.selected_overlay)
                } else {
                    holder.itemView.setBackgroundResource(backgroundRes)
                }

                val favicon = if (item.isUseFavicon) faviconManager[item.url] else null
                if (favicon != null) {
                    holder.icon.setFavicon(favicon)
                } else {
                    holder.icon.setSearchUrl(item)
                }
                holder.textView.text = item.title
            }

            override fun getItemCount() = providers.size

            val backgroundRes: Int

            init {
                val value = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
                backgroundRes = value.resourceId
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlHolder {
                return UrlHolder(inflater.inflate(R.layout.serach_url_spinner, parent, false), listener)
            }

            class UrlHolder(view: View, listener: OnRecyclerListener) : RecyclerView.ViewHolder(view) {
                val icon = view.findViewById<SearchSimpleIconView>(R.id.iconColorView)!!
                val textView = view.findViewById<TextView>(R.id.titleTextView)!!

                init {

                    itemView.setOnClickListener { v -> listener.onRecyclerItemClicked(v, adapterPosition) }

                    itemView.setOnLongClickListener { v -> listener.onRecyclerItemLongClicked(v, adapterPosition) }
                }
            }
        }
    }
}
