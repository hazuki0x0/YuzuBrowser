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

package jp.hazuki.yuzubrowser.legacy.search.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.RecyclerMenu
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import javax.inject.Inject

class SearchUrlListFragment : DaggerFragment(), SearchSettingDialog.OnUrlEditedListener, OnRecyclerListener, RecyclerMenu.OnRecyclerMoveListener, RecyclerMenu.OnRecyclerMenuListener, DeleteDialogCompat.OnDelete {

    private lateinit var adapter: UrlAdapter
    private lateinit var manager: SearchUrlManager
    private lateinit var rootView: View
    private var edited = false

    @Inject
    lateinit var moshi: Moshi

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        rootView = view
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(activity))

        val touchHelper = ItemTouchHelper(Touch())
        touchHelper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(touchHelper)

        manager = SearchUrlManager(activity, moshi)
        adapter = UrlAdapter(activity, manager, this, FaviconManager.getInstance(activity), this)

        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.fab).setOnClickListener {
            SearchSettingDialog.newInstance(-1, null).show(childFragmentManager, "edit")
        }
    }

    override fun onPause() {
        super.onPause()
        if (edited) {
            manager.save()
            edited = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_with_fab, container, false)
    }


    override fun onRecyclerItemClicked(v: View, position: Int) {
        SearchSettingDialog.newInstance(position, manager[position]).show(childFragmentManager, "edit")
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean = false

    override fun onUrlEdited(index: Int, url: SearchUrl) {
        edited = true
        if (index >= 0) {
            manager[index] = url
            adapter.notifyItemChanged(index)
        } else {
            manager.add(url)
            adapter.notifyItemInserted(manager.size - 1)
        }
    }

    override fun onDeleteClicked(position: Int) {
        if (manager.size > 1) {
            DeleteDialogCompat.newInstance(context, R.string.confirm, R.string.confirm_delete_search_url, position)
                    .show(childFragmentManager, "delete")
        }
    }

    override fun onDelete(position: Int) {
        if (manager.size > 1) {
            edited = true
            manager.removeAt(position)
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
        if (position < manager.size - 1) {
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
        override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
            return if (manager.size > 1) {
                ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or
                        ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            } else {
                0
            }
        }

        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            edited = true
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
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