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

package jp.hazuki.yuzubrowser.history

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.*
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.bookmark.view.showAddBookmarkDialog
import jp.hazuki.yuzubrowser.browser.BrowserManager
import jp.hazuki.yuzubrowser.browser.openable.OpenUrl
import jp.hazuki.yuzubrowser.browser.openable.OpenUrlList
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.WebUtils
import jp.hazuki.yuzubrowser.utils.extensions.setClipboardWithToast
import jp.hazuki.yuzubrowser.utils.view.recycler.RecyclerTouchLocationDetector
import kotlinx.android.synthetic.main.recycler_view.*
import java.util.*


class BrowserHistoryFragment : Fragment(), BrowserHistoryAdapter.OnHistoryRecyclerListener, ActionMode.Callback {

    private var pickMode: Boolean = false
    private lateinit var adapter: BrowserHistoryAdapter
    private lateinit var manager: BrowserHistoryManager
    private val locationDetector = RecyclerTouchLocationDetector()

    private var searchView: SearchView? = null

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.recycler_view, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        pickMode = arguments.getBoolean(PICK_MODE)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(object : BrowserHistoryScrollListener(layoutManager) {
            override fun onLoadMore(current_page: Int) {
                adapter.loadMore()
                recyclerView.post { adapter.notifyDataSetChanged() }
            }
        })

        manager = BrowserHistoryManager.getInstance(activity)
        adapter = BrowserHistoryAdapter(activity, manager, pickMode, this)
        val decoration = StickyHeaderDecoration(adapter)
        adapter.setDecoration(decoration)
        recyclerView.addItemDecoration(decoration)
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(locationDetector)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        sendUrl(adapter.getItem(position), AppData.newtab_history.get())
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        if (!pickMode) {
            val history = adapter.getItem(position)
            val url = history.url
            val title = history.title

            val popupMenu = PopupMenu(activity, v, locationDetector.gravity)
            val menu = popupMenu.menu
            menu.add(R.string.open).setOnMenuItemClickListener {
                sendUrl(url, BrowserManager.LOAD_URL_TAB_CURRENT)
                false
            }
            menu.add(R.string.open_new).setOnMenuItemClickListener {
                sendUrl(url, BrowserManager.LOAD_URL_TAB_NEW)
                false
            }
            menu.add(R.string.open_bg).setOnMenuItemClickListener {
                sendUrl(url, BrowserManager.LOAD_URL_TAB_BG)
                false
            }
            menu.add(R.string.open_new_right).setOnMenuItemClickListener {
                sendUrl(url, BrowserManager.LOAD_URL_TAB_NEW_RIGHT)
                false
            }
            menu.add(R.string.open_bg_right).setOnMenuItemClickListener {
                sendUrl(url, BrowserManager.LOAD_URL_TAB_BG_RIGHT)
                false
            }
            menu.add(R.string.add_bookmark).setOnMenuItemClickListener {
                showAddBookmarkDialog(activity, childFragmentManager, title, url)
                false
            }
            menu.add(R.string.delete_history).setOnMenuItemClickListener {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_history)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            manager.delete(url)

                            adapter.remove(position)
                            adapter.notifyDataSetChanged()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                false
            }
            menu.add(R.string.share).setOnMenuItemClickListener {
                WebUtils.shareWeb(activity, url, title)
                false
            }
            menu.add(R.string.copy_url).setOnMenuItemClickListener {
                activity.setClipboardWithToast(url)
                false
            }

            popupMenu.show()
        }
        return true
    }

    override fun onIconClicked(v: View, position: Int) {
        sendUrl(adapter.getItem(position), BrowserManager.LOAD_URL_TAB_NEW)
    }

    private fun sendUrl(history: BrowserHistory, target: Int) {
        if (pickMode) {
            sendPicked(history)
        } else {
            sendUrl(history.url, target)
        }
    }

    private fun sendUrl(url: String?, target: Int) {
        if (url != null) {
            val intent = Intent()
            intent.putExtra(BrowserManager.EXTRA_OPENABLE, OpenUrl(url, target))
            activity.setResult(RESULT_OK, intent)
        }
        activity.finish()
    }

    private fun sendPicked(history: BrowserHistory) {
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_TITLE, history.title)
        intent.putExtra(Intent.EXTRA_TEXT, history.url)
        intent.putExtra(Intent.EXTRA_STREAM, FaviconManager.getInstance(activity).getFaviconBytes(history.url))
        activity.setResult(RESULT_OK, intent)
        activity.finish()
    }

    fun onBackPressed(): Boolean {
        if (searchView != null && !searchView!!.isIconified) {
            searchView!!.isIconified = true
            return true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history, menu)

        val menuItem = menu.findItem(R.id.search_history)

        searchView = (menuItem.actionView as SearchView).apply {
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchView!!.clearFocus()
                    if (!TextUtils.isEmpty(query)) {
                        adapter.search(query)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
            setOnCloseListener {
                adapter.reLoad()
                false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_all_favicon -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_favicon)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            FaviconManager.getInstance(activity).clear()
                            adapter.notifyDataSetChanged()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                return true
            }
            R.id.delete_all_histories -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_history)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            manager.deleteAll()

                            adapter.reLoad()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                return true
            }
            R.id.delete_all_displayed_item -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_displayed_item)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            if (searchView!!.isIconified || TextUtils.isEmpty(searchView!!.query)) {
                                manager.deleteAll()
                                adapter.reLoad()
                            } else {
                                val query = searchView!!.query.toString()
                                manager.deleteWithSearch(query)
                                adapter.search(query)
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                return true
            }
            R.id.multiSelect -> {
                val next = !adapter.isMultiSelectMode
                adapter.isMultiSelectMode = next
                actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
                if (!next && actionMode != null) {
                    actionMode!!.finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        mode.menuInflater.inflate(R.menu.history_action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteSelectedHistories -> {
                val items = adapter.selectedItems
                Collections.sort(items, Collections.reverseOrder())
                items
                        .map { adapter.remove(it) }
                        .forEach { manager.delete(it.url) }
                adapter.notifyDataSetChanged()
                mode.finish()
            }
            R.id.openAllNew -> openUrls(adapter.selectedItems, BrowserManager.LOAD_URL_TAB_NEW)
            R.id.openAllBg -> openUrls(adapter.selectedItems, BrowserManager.LOAD_URL_TAB_BG)
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter.isMultiSelectMode = false
    }

    private fun openUrls(items: List<Int>?, target: Int) {
        if (items != null && items.isNotEmpty()) {
            val urls = items.map { adapter.getItem(it).url }

            val intent = Intent()
            intent.putExtra(BrowserManager.EXTRA_OPENABLE, OpenUrlList(urls, target))
            activity.setResult(RESULT_OK, intent)
            activity.finish()
        }
    }

    companion object {
        private const val PICK_MODE = "pick"

        fun newInstance(isPickMode: Boolean): BrowserHistoryFragment {
            return BrowserHistoryFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PICK_MODE, isPickMode)
                }
            }
        }
    }
}
