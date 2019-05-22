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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.blogic.ISearchUrlRepository
import jp.hazuki.yuzubrowser.search.model.provider.SearchSuggestProviders
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl
import jp.hazuki.yuzubrowser.search.presentation.widget.SearchSimpleIconView
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import jp.hazuki.yuzubrowser.ui.widget.recycler.RecyclerMenu
import javax.inject.Inject

class SearchUrlListFragment : DaggerFragment(), SearchSettingDialog.OnUrlEditedListener, OnRecyclerListener, RecyclerMenu.OnRecyclerMoveListener, RecyclerMenu.OnRecyclerMenuListener, DeleteDialogCompat.OnDelete {

    private lateinit var adapter: UrlAdapter
    private lateinit var rootView: View
    private lateinit var providers: SearchSuggestProviders
    private var edited = false

    @Inject
    lateinit var moshi: Moshi
    @Inject
    lateinit var manager: ISearchUrlRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        rootView = view
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val touchHelper = ItemTouchHelper(Touch())
        touchHelper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(touchHelper)
        providers = SearchSuggestProviders(manager.load())

        adapter = UrlAdapter(activity, providers.items, this, FaviconManager.getInstance(activity), this)

        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.fab).setOnClickListener {
            SearchSettingDialog.newInstance(-1, null).show(childFragmentManager, "edit")
        }
    }

    override fun onPause() {
        super.onPause()
        if (edited) {
            manager.save(providers.toSettings())
            edited = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search_settings_fragment, container, false)
    }


    override fun onRecyclerItemClicked(v: View, position: Int) {
        SearchSettingDialog.newInstance(position, providers[position]).show(childFragmentManager, "edit")
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean = false

    override fun onUrlEdited(index: Int, url: SearchUrl) {
        edited = true
        if (index >= 0) {
            providers[index] = url
            adapter.notifyItemChanged(index)
        } else {
            providers.add(url)
            adapter.notifyItemInserted(providers.size - 1)
        }
    }

    override fun onDeleteClicked(position: Int) {
        if (providers.size > 1) {
            DeleteDialogCompat.newInstance(context, R.string.confirm, R.string.confirm_delete_search_url, position)
                .show(childFragmentManager, "delete")
        }
    }

    override fun onDelete(position: Int) {
        if (providers.size > 1) {
            edited = true
            providers.removeAt(position)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMoveUp(position: Int) {
        if (position > 0) {
            edited = true
            adapter.move(position, position - 1)
        }
    }

    override fun onMoveDown(position: Int) {
        if (position < providers.size - 1) {
            edited = true
            adapter.move(position, position + 1)
        }
    }

    private class UrlAdapter(val context: Context, list: MutableList<SearchUrl>, val fragment: SearchUrlListFragment, val faviconManager: FaviconManager, listener: OnRecyclerListener) : ArrayRecyclerAdapter<SearchUrl, UrlAdapter.UrlHolder>(context, list, listener) {

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): UrlHolder {
            return UrlHolder(inflater.inflate(R.layout.search_url_list_edit_item, parent, false), this)
        }

        override fun onBindViewHolder(holder: UrlHolder, item: SearchUrl, position: Int) {
            holder.menu.setOnClickListener {
                RecyclerMenu(context, it, holder.adapterPosition, fragment, fragment).show()
            }
        }

        class UrlHolder(view: View, val adapter: UrlAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<SearchUrl>(view, adapter) {
            val icon: SearchSimpleIconView = view.findViewById(R.id.iconColorView)
            val textView: TextView = view.findViewById(R.id.titleTextView)
            val url: TextView = view.findViewById(R.id.urlTextView)
            val menu: ImageButton = view.findViewById(R.id.menuImageButton)

            override fun setUp(item: SearchUrl) {
                super.setUp(item)
                val favicon = if (item.isUseFavicon) adapter.faviconManager[item.url] else null
                if (favicon != null) {
                    icon.setFavicon(favicon)
                } else {
                    icon.setSearchUrl(item)
                }
                textView.text = item.title
                url.text = item.url
            }
        }
    }

    private inner class Touch : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (providers.size > 1) {
                makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            } else {
                0
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            edited = true
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            edited = true
            val position = viewHolder.adapterPosition
            val searchUrl = adapter.remove(position)

            Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    edited = true
                    adapter.add(position, searchUrl)
                    adapter.notifyItemInserted(position)
                }
                .show()
        }

    }
}
