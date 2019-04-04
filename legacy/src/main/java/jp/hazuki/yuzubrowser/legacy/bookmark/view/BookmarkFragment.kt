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

package jp.hazuki.yuzubrowser.legacy.bookmark.view

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkSite
import jp.hazuki.yuzubrowser.legacy.browser.BrowserManager
import jp.hazuki.yuzubrowser.legacy.browser.openable.OpenUrl
import jp.hazuki.yuzubrowser.legacy.browser.openable.OpenUrlList
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.utils.PackageUtils
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import jp.hazuki.yuzubrowser.legacy.utils.extensions.setClipboardWithToast
import jp.hazuki.yuzubrowser.ui.app.LongPressFixActivity
import jp.hazuki.yuzubrowser.ui.extensions.addOnBackPressedCallback
import jp.hazuki.yuzubrowser.ui.widget.breadcrumbs.BreadcrumbsView
import jp.hazuki.yuzubrowser.ui.widget.breadcrumbs.BreadcrumbsViewAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.RecyclerTouchLocationDetector
import kotlinx.android.synthetic.main.fragment_bookmark.*
import java.util.*

class BookmarkFragment : Fragment(), BookmarkItemAdapter.OnBookmarkRecyclerListener, ActionMode.Callback, BreadcrumbsView.OnBreadcrumbsViewClickListener {

    private var pickMode: Boolean = false

    private lateinit var adapter: BookmarkItemAdapter
    private lateinit var mManager: BookmarkManager
    private lateinit var mCurrentFolder: BookmarkFolder

    private lateinit var locationDetector: RecyclerTouchLocationDetector

    private var actionMode: ActionMode? = null

    private val root: BookmarkFolder
        get() = mManager.root

    private var showBreadCrumb = true

    private lateinit var breadcrumbAdapter: BreadcrumbsViewAdapter<BookmarkCrumb>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return
        val arguments = arguments ?: throw IllegalArgumentException()

        (activity as AppCompatActivity).run {
            setSupportActionBar(tool_bar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity)
        val helper = ItemTouchHelper(Touch())
        helper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(helper)

        pickMode = arguments.getBoolean(MODE_PICK)

        mManager = BookmarkManager.getInstance(activity)

        locationDetector = RecyclerTouchLocationDetector()

        recyclerView.addOnItemTouchListener(locationDetector)

        breadcrumbAdapter = BreadcrumbsViewAdapter(activity, breadCrumbsView)
        breadCrumbsView.adapter = breadcrumbAdapter

        val firstPos = getFirstPosition(arguments)

        toolbar_appbar.elevation = 0f
        addAllFolderCrumbs(firstPos)
        breadCrumbsView.listener = this

        fastScroller.let {
            it.attachRecyclerView(recyclerView)
            it.attachAppBarLayout(coordinator, subBar)
        }

        setList(firstPos)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.addOnBackPressedCallback(this) {
            if (adapter.isSortMode) {
                adapter.isSortMode = false
                Toast.makeText(activity, R.string.end_sort, Toast.LENGTH_SHORT).show()
                return@addOnBackPressedCallback true
            } else if (adapter.isMultiSelectMode) {
                adapter.isMultiSelectMode = false
                return@addOnBackPressedCallback true
            }
            val parent = mCurrentFolder.parent
            return@addOnBackPressedCallback if (parent != null) {
                setList(parent)
                breadcrumbAdapter.select(breadcrumbAdapter.selectedIndex - 1)
                subBar.setExpanded(true, false)
                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (showBreadCrumb) {
            toolbar_appbar.elevation = 0f
        } else {
            toolbar_appbar.elevation = subBar.elevation
        }
    }

    override fun onBreadcrumbItemClick(position: Int) {
        setList(breadcrumbAdapter.crumbs[position].folder)
        breadcrumbAdapter.select(position)
    }

    private fun setList(folder: BookmarkFolder) {
        val activity = activity ?: return
        mCurrentFolder = folder
        setTitle(folder)

        adapter = BookmarkItemAdapter(activity, folder.itemList, pickMode, AppData.open_bookmark_new_tab.get(), this)
        recyclerView.adapter = adapter
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val item = mCurrentFolder[position]
        when (item) {
            is BookmarkSite -> {
                if (pickMode) {
                    pickBookmark(item)
                } else {
                    sendUrl(item.url, AppData.newtab_bookmark.get())
                }
                activity?.finish()
            }
            is BookmarkFolder -> {
                setList(item)
                breadcrumbAdapter.addItem(getCrumb(item))
                subBar.setExpanded(true, false)
            }
            else -> throw IllegalStateException("Unknown BookmarkItem type")
        }
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        return if (!adapter.isSortMode && !pickMode) {
            if (adapter.isMultiSelectMode) {
                adapter.toggle(position)
            } else {
                (activity as AppCompatActivity).startSupportActionMode(this)
                adapter.isMultiSelectMode = true
                adapter.setSelect(position, true)
            }
            true
        } else {
            false
        }
    }

    override fun onIconClick(v: View, position: Int) {
        val item = adapter[position]
        if (item is BookmarkSite) {
            sendUrl(item.url, AppData.open_bookmark_icon_action.get())
        }
    }

    override fun onShowMenu(v: View, position: Int) {
        showContextMenu(v, position)
    }

    override fun onCancelMultiSelectMode() {
        actionMode?.finish()
    }

    private fun sendUrl(url: String?, target: Int) {
        val activity = activity ?: return

        if (url != null) {
            activity.setResult(RESULT_OK, Intent().apply {
                putExtra(BrowserManager.EXTRA_OPENABLE, OpenUrl(url, target))
            })
        }
        activity.finish()
    }

    private fun sendUrl(list: Collection<BookmarkItem>?, target: Int) {
        val activity = activity ?: return

        if (list != null) {
            val urlList = list.filterIsInstance<BookmarkSite>().map { it.url }

            if (urlList.isEmpty())
                return

            activity.setResult(RESULT_OK, Intent().apply {
                putExtra(BrowserManager.EXTRA_OPENABLE, OpenUrlList(urlList, target))
            })
        }
        activity.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!pickMode) {
            inflater.inflate(R.menu.bookmark, menu)
            val breadcrumbs = menu.findItem(R.id.breadcrumbs)
            val show = AppData.bookmark_breadcrumbs.get()
            breadcrumbs.isChecked = show
            showBreadCrumbs(show)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity ?: return false

        when (item.itemId) {
            R.id.addFolder -> {
                AddBookmarkFolderDialog(activity, mManager, getString(R.string.new_folder_name), mCurrentFolder)
                        .setOnClickListener(DialogInterface.OnClickListener { _, _ -> adapter.notifyDataSetChanged() })
                        .show()
                return true
            }
            R.id.sort -> {
                val next = !adapter.isSortMode
                adapter.isSortMode = next

                Toast.makeText(activity, if (next) R.string.start_sort else R.string.end_sort, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.breadcrumbs -> {
                val checked = !item.isChecked
                item.isChecked = checked
                AppData.bookmark_breadcrumbs.set(checked)
                AppData.commit(activity, AppData.bookmark_breadcrumbs)
                showBreadCrumbs(checked)
                return true
            }
        }
        return false
    }

    private fun getSelectedBookmarks(items: List<Int>): List<BookmarkItem> = items.map { mCurrentFolder[it] }

    private fun showContextMenu(v: View, index: Int) {
        val activity = activity ?: return

        val menu = PopupMenu(activity, v, locationDetector.gravity)
        val inflater = menu.menuInflater
        val bookmarkItem: BookmarkItem?
        if (pickMode) {
            bookmarkItem = adapter[index]
            menu.menu.add(R.string.select_this_item).setOnMenuItemClickListener {

                if (bookmarkItem is BookmarkSite) {
                    pickBookmark(bookmarkItem)
                } else {
                    val sender = Intent(activity, BookmarkActivity::class.java)
                    sender.putExtra("id", bookmarkItem.id)

                    val intent = Intent()
                    intent.putExtra(Intent.EXTRA_TITLE, bookmarkItem.title)
                    intent.putExtra(Intent.EXTRA_TEXT, sender.toUri(Intent.URI_INTENT_SCHEME))
                    activity.setResult(RESULT_OK, intent)
                }
                activity.finish()
                false
            }
        } else {
            bookmarkItem = when {
                adapter.isMultiSelectMode -> {
                    inflater.inflate(R.menu.bookmark_multiselect_menu, menu.menu)
                    null
                }
                mCurrentFolder[index] is BookmarkSite -> {
                    inflater.inflate(R.menu.bookmark_site_menu, menu.menu)
                    adapter[index]
                }
                else -> {
                    inflater.inflate(R.menu.bookmark_folder_menu, menu.menu)
                    adapter[index]
                }
            }
        }

        menu.setOnMenuItemClickListener { item ->
            onContextMenuClick(item.itemId, bookmarkItem, index)
            true
        }
        menu.show()
    }

    private fun pickBookmark(site: BookmarkSite) {
        val activity = activity ?: return

        val intent = Intent()
        intent.putExtra(Intent.EXTRA_TITLE, site.title)
        intent.putExtra(Intent.EXTRA_TEXT, site.url)

        val icon = adapter.getFavicon(site)
        if (icon != null) {
            intent.putExtra(Intent.EXTRA_STREAM, icon)
        }

        activity.setResult(RESULT_OK, intent)
    }

    private fun onContextMenuClick(id: Int, item: BookmarkItem?, index: Int) {
        val activity = activity ?: return

        when (id) {
            R.id.open -> sendUrl((item as BookmarkSite).url, BrowserManager.LOAD_URL_TAB_CURRENT)
            R.id.openNew -> sendUrl((item as BookmarkSite).url, BrowserManager.LOAD_URL_TAB_NEW)
            R.id.openBg -> sendUrl((item as BookmarkSite).url, BrowserManager.LOAD_URL_TAB_BG)
            R.id.openNewRight -> sendUrl((item as BookmarkSite).url, BrowserManager.LOAD_URL_TAB_NEW_RIGHT)
            R.id.openBgRight -> sendUrl((item as BookmarkSite).url, BrowserManager.LOAD_URL_TAB_BG_RIGHT)
            R.id.share -> {
                val site = item as BookmarkSite
                WebUtils.shareWeb(activity, site.url, site.title)
            }
            R.id.copyUrl -> activity.setClipboardWithToast((item as BookmarkSite).url)
            R.id.addToHome -> {
                val url = (item as BookmarkSite).url
                val bitmap = FaviconManager.getInstance(activity)[url]
                PackageUtils.createShortcut(activity, item.title, url, bitmap)
            }
            R.id.editBookmark -> if (item is BookmarkSite) {
                AddBookmarkSiteDialog(activity, mManager, item)
                        .setOnClickListener { _, _ -> adapter.notifyDataSetChanged() }
                        .show()
            } else if (item is BookmarkFolder) {
                AddBookmarkFolderDialog(activity, mManager, item)
                        .setOnClickListener(DialogInterface.OnClickListener { _, _ -> adapter.notifyDataSetChanged() })
                        .show()
            }
            R.id.moveBookmark -> BookmarkFoldersDialog(activity, mManager)
                    .setTitle(R.string.move_bookmark)
                    .setCurrentFolder(mCurrentFolder, item)
                    .setOnFolderSelectedListener { _, folder ->
                        mManager.moveTo(mCurrentFolder, folder, index)

                        mManager.save()
                        adapter.notifyDataSetChanged()
                        false
                    }
                    .show()
            R.id.moveUp -> if (index > 0) {
                Collections.swap(mCurrentFolder.itemList, index - 1, index)
                mManager.save()
                adapter.notifyDataSetChanged()
            }
            R.id.moveDown -> if (index < mCurrentFolder.size() - 1) {
                Collections.swap(mCurrentFolder.itemList, index + 1, index)
                mManager.save()
                adapter.notifyDataSetChanged()
            }
            R.id.deleteBookmark -> AlertDialog.Builder(activity)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_delete_bookmark)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        mManager.remove(mCurrentFolder, index)
                        mManager.save()
                        adapter.notifyDataSetChanged()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            R.id.openAllNew -> {
                val items = if (item is BookmarkFolder) {
                    item.itemList
                } else {
                    getSelectedBookmarks(adapter.selectedItems)
                }
                sendUrl(items, BrowserManager.LOAD_URL_TAB_NEW)
            }
            R.id.openAllBg -> {
                val items = if (item is BookmarkFolder) {
                    item.itemList
                } else {
                    getSelectedBookmarks(adapter.selectedItems)
                }
                sendUrl(items, BrowserManager.LOAD_URL_TAB_BG)
            }
            R.id.moveAllBookmark -> {
                val bookmarkItems = getSelectedBookmarks(adapter.selectedItems)
                adapter.isMultiSelectMode = false
                BookmarkFoldersDialog(activity, mManager)
                        .setTitle(R.string.move_bookmark)
                        .setCurrentFolder(mCurrentFolder, bookmarkItems)
                        .setOnFolderSelectedListener { _, folder ->
                            mManager.moveAll(mCurrentFolder, folder, bookmarkItems)

                            mManager.save()
                            adapter.notifyDataSetChanged()
                            false
                        }
                        .show()
            }
            R.id.deleteAllBookmark -> AlertDialog.Builder(activity)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_delete_bookmark)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val selectedList = getSelectedBookmarks(adapter.selectedItems)

                        mManager.removeAll(mCurrentFolder, selectedList)
                        mManager.save()

                        adapter.isMultiSelectMode = false
                        adapter.notifyDataSetChanged()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
    }

    override fun onSelectionStateChange(items: Int) {
        actionMode?.title = getString(R.string.accessibility_toolbar_multi_select, items)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.bookmark_action_mode, menu)
        actionMode = mode
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.openAllNew -> {
                val items = getSelectedBookmarks(adapter.selectedItems)
                sendUrl(items, BrowserManager.LOAD_URL_TAB_NEW)
                return true
            }
            R.id.openAllBg -> {
                val items = getSelectedBookmarks(adapter.selectedItems)
                sendUrl(items, BrowserManager.LOAD_URL_TAB_BG)
                return true
            }
            R.id.selectAll -> {
                for (i in 0 until adapter.itemCount) {
                    adapter.setSelect(i, true)
                }
                return true
            }
            R.id.moveAllBookmark -> {
                val bookmarkItems = getSelectedBookmarks(adapter.selectedItems)
                BookmarkFoldersDialog(activity, mManager)
                        .setTitle(R.string.move_bookmark)
                        .setCurrentFolder(mCurrentFolder, bookmarkItems)
                        .setOnFolderSelectedListener { _, folder ->
                            mManager.moveAll(mCurrentFolder, folder, bookmarkItems)

                            mManager.save()
                            mode.finish()
                            false
                        }
                        .show()
                return true
            }
            R.id.deleteAllBookmark -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_bookmark)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val selectedList = getSelectedBookmarks(adapter.selectedItems)

                            mManager.removeAll(mCurrentFolder, selectedList)
                            mManager.save()

                            mode.finish()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        (activity as LongPressFixActivity).onDestroyActionMode()
        adapter.isMultiSelectMode = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (AppData.save_bookmark_folder.get()) {
            AppData.save_bookmark_folder_id.set(mCurrentFolder.id)
            AppData.commit(activity, AppData.save_bookmark_folder_id)
        }
    }

    private inner class Touch : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            mManager.save()
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun isLongPressDragEnabled(): Boolean {
            return adapter.isSortMode
        }
    }

    private fun showBreadCrumbs(show: Boolean) {
        if (show) {
            if (subBar.childCount == 0) {
                subBar.addView(breadCrumbsView)
            }
        } else {
            if (subBar.childCount == 1) {
                subBar.removeView(breadCrumbsView)
            }
        }
        showBreadCrumb = show
        setTitle(mCurrentFolder)
        if (show) {
            toolbar_appbar.elevation = 0f
        } else {
            toolbar_appbar.elevation = subBar.elevation
        }
    }

    private fun setTitle(folder: BookmarkFolder) {
        val actionBar = (activity as AppCompatActivity).supportActionBar ?: return
        if (showBreadCrumb) {
            actionBar.title = ""
        } else {
            if (folder.title.isNullOrEmpty()) {
                actionBar.setTitle(R.string.bookmark)
            } else {
                actionBar.title = folder.title
            }
        }
    }

    private fun getFirstPosition(arguments: Bundle): BookmarkFolder {
        var id = arguments.getLong(ITEM_ID)
        if (AppData.save_bookmark_folder.get() || id > 0) {
            if (id < 1) {
                id = AppData.save_bookmark_folder_id.get()
            }
            val item = mManager[id]
            if (item is BookmarkFolder) {
                return item
            }
        }
        return root
    }

    private fun getCrumb(folder: BookmarkFolder): BookmarkCrumb {
        val title = folder.title
        return BookmarkCrumb(folder, if (title.isNullOrEmpty()) getString(R.string.bookmark) else title)
    }

    private fun addAllFolderCrumbs(target: BookmarkFolder) {
        val parent = target.parent
        if (parent != null) addAllFolderCrumbs(parent)
        breadcrumbAdapter.addItem(getCrumb(target))
    }

    private class BookmarkCrumb(val folder: BookmarkFolder, override val title: String) : BreadcrumbsView.Breadcrumb {
        override fun equals(other: Any?): Boolean {
            return other is BookmarkCrumb && other.folder == folder
        }

        override fun hashCode(): Int {
            return folder.hashCode()
        }
    }

    companion object {
        private const val MODE_PICK = "pick"
        private const val ITEM_ID = "id"

        operator fun invoke(pickMode: Boolean, id: Long): BookmarkFragment {
            val fragment = BookmarkFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(MODE_PICK, pickMode)
                putLong(ITEM_ID, id)
            }
            return fragment
        }
    }
}
