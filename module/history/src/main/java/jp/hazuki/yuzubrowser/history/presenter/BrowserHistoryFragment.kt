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

package jp.hazuki.yuzubrowser.history.presenter

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.bookmark.view.showAddBookmarkDialog
import jp.hazuki.yuzubrowser.browser.connecter.openable.OpenUrl
import jp.hazuki.yuzubrowser.browser.connecter.openable.OpenUrlList
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryManager
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryModel
import jp.hazuki.yuzubrowser.historyModel.R
import jp.hazuki.yuzubrowser.ui.*
import jp.hazuki.yuzubrowser.ui.extensions.addCallback
import jp.hazuki.yuzubrowser.ui.extensions.setClipboardWithToast
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.widget.recycler.LoadMoreListener
import jp.hazuki.yuzubrowser.ui.widget.recycler.RecyclerTouchLocationDetector
import kotlinx.android.synthetic.main.fragment_history.*
import org.jetbrains.anko.share
import java.util.*
import javax.inject.Inject


class BrowserHistoryFragment : DaggerFragment(), BrowserHistoryAdapter.OnHistoryRecyclerListener, ActionMode.Callback {

    private var pickMode: Boolean = false
    private lateinit var adapter: BrowserHistoryAdapter
    private lateinit var manager: BrowserHistoryManager
    private val locationDetector = RecyclerTouchLocationDetector()

    private var searchView: SearchView? = null

    private var actionMode: ActionMode? = null

    @Inject
    lateinit var faviconManager: FaviconManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        val arguments = arguments ?: throw IllegalArgumentException()

        pickMode = arguments.getBoolean(PICK_MODE)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val listener = object : LoadMoreListener(layoutManager) {
            override fun onLoadMore(current_page: Int) {
                adapter.loadMore()
                recyclerView.post { adapter.notifyDataSetChanged() }
            }
        }
        recyclerView.addOnScrollListener(listener)
        touchScrollBar.addScrollListener(listener)

        manager = BrowserHistoryManager.getInstance(activity)
        adapter = BrowserHistoryAdapter(activity, manager, faviconManager, pickMode, this)
        val decoration = StickyHeaderDecoration(adapter)
        adapter.setDecoration(decoration)
        recyclerView.addItemDecoration(decoration)
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(locationDetector)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val searchView = searchView
            return@addCallback if (searchView != null && !searchView.isIconified) {
                searchView.isIconified = true
                true
            } else {
                false
            }
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        if (adapter.isMultiSelectMode) {
            adapter.toggle(position)
        } else {
            sendUrl(adapter.getItem(position), AppPrefs.newtabHistory.get())
        }
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        if (!pickMode) {
            if (adapter.isMultiSelectMode) {
                adapter.toggle(position)
            } else {
                val activity = activity ?: return false
                (activity as AppCompatActivity).startSupportActionMode(this)
                adapter.isMultiSelectMode = true
                adapter.setSelect(position, true)
            }
        }
        return true
    }

    override fun onShowMenu(v: View, position: Int) {
        val activity = activity ?: return

        if (!pickMode) {
            val history = adapter.getItem(position)
            val url = history.url ?: return
            val title = history.title ?: return

            val popupMenu = PopupMenu(activity, v, locationDetector.gravity)
            val menu = popupMenu.menu
            menu.add(R.string.open).setOnMenuItemClickListener {
                sendUrl(url, BROWSER_LOAD_URL_TAB_CURRENT)
                false
            }
            menu.add(R.string.open_new).setOnMenuItemClickListener {
                sendUrl(url, BROWSER_LOAD_URL_TAB_NEW)
                false
            }
            menu.add(R.string.open_bg).setOnMenuItemClickListener {
                sendUrl(url, BROWSER_LOAD_URL_TAB_BG)
                false
            }
            menu.add(R.string.open_new_right).setOnMenuItemClickListener {
                sendUrl(url, BROWSER_LOAD_URL_TAB_NEW_RIGHT)
                false
            }
            menu.add(R.string.open_bg_right).setOnMenuItemClickListener {
                sendUrl(url, BROWSER_LOAD_URL_TAB_BG_RIGHT)
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
                activity.share(url, title)
                false
            }
            menu.add(R.string.copy_url).setOnMenuItemClickListener {
                activity.setClipboardWithToast(url)
                false
            }

            popupMenu.show()
        }
    }

    override fun onIconClicked(v: View, position: Int) {
        sendUrl(adapter.getItem(position), BROWSER_LOAD_URL_TAB_NEW)
    }

    private fun sendUrl(historyModel: BrowserHistoryModel, target: Int) {
        if (pickMode) {
            sendPicked(historyModel)
        } else {
            sendUrl(historyModel.url, target)
        }
    }

    private fun sendUrl(url: String?, target: Int) {
        val activity = activity ?: return

        if (url != null) {
            val intent = Intent()
            intent.putExtra(INTENT_EXTRA_OPENABLE, OpenUrl(url, target))
            activity.setResult(RESULT_OK, intent)
        }
        activity.finish()
    }

    private fun sendPicked(historyModel: BrowserHistoryModel) {
        val activity = activity ?: return

        val intent = Intent()
        intent.putExtra(Intent.EXTRA_TITLE, historyModel.title)
        intent.putExtra(Intent.EXTRA_TEXT, historyModel.url)
        intent.putExtra(Intent.EXTRA_STREAM,
            historyModel.url?.let { faviconManager.getFaviconBytes(it) })
        activity.setResult(RESULT_OK, intent)
        activity.finish()
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
        val activity = activity ?: return false

        when (item.itemId) {
            R.id.delete_all_favicon -> {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_delete_all_favicon)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        faviconManager.clear()
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCancelMultiSelectMode() {
        actionMode?.finish()
    }

    override fun onSelectionStateChange(items: Int) {
        actionMode?.title = getString(R.string.accessibility_toolbar_multi_select, items)
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
            R.id.openAllNew -> openUrls(adapter.selectedItems, BROWSER_LOAD_URL_TAB_NEW)
            R.id.openAllBg -> openUrls(adapter.selectedItems, BROWSER_LOAD_URL_TAB_BG)
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter.isMultiSelectMode = false
    }

    private fun openUrls(items: List<Int>?, target: Int) {
        val activity = activity ?: return

        if (items != null && items.isNotEmpty()) {
            val urls = items.mapNotNull { adapter.getItem(it).url }

            val intent = Intent()
            intent.putExtra(INTENT_EXTRA_OPENABLE, OpenUrlList(urls, target))
            activity.setResult(RESULT_OK, intent)
            activity.finish()
        }
    }

    companion object {
        private const val PICK_MODE = "pick"

        operator fun invoke(isPickMode: Boolean): BrowserHistoryFragment {
            return BrowserHistoryFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PICK_MODE, isPickMode)
                }
            }
        }
    }
}
