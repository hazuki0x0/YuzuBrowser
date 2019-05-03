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

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.ArrayMap
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration
import jp.hazuki.yuzubrowser.core.utility.extensions.intentFor
import jp.hazuki.yuzubrowser.core.utility.extensions.resolvePath
import jp.hazuki.yuzubrowser.download.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.utils.checkFlag
import jp.hazuki.yuzubrowser.download.core.utils.getFile
import jp.hazuki.yuzubrowser.download.createFileOpenIntent
import jp.hazuki.yuzubrowser.download.reDownload
import jp.hazuki.yuzubrowser.download.service.DownloadDatabase
import jp.hazuki.yuzubrowser.download.service.connection.ActivityClient
import jp.hazuki.yuzubrowser.download.ui.DownloadCommandController
import jp.hazuki.yuzubrowser.ui.ACTIVITY_MAIN_BROWSER
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import jp.hazuki.yuzubrowser.ui.extensions.addCallback
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.LoadMoreListener
import org.jetbrains.anko.longToast

class DownloadListFragment : Fragment(), ActivityClient.ActivityClientListener, OnRecyclerMenuListener, ActionMode.Callback {

    private var commandController: DownloadCommandController? = null
    private lateinit var adapter: DownloadListAdapter
    private lateinit var database: DownloadDatabase

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.addOnScrollListener(object : LoadMoreListener(layoutManager) {
            override fun onLoadMore(current_page: Int) {
                adapter.loadMore()
            }
        })
        recyclerView.addItemDecoration(DividerItemDecoration(activity))
        recyclerView.layoutManager = layoutManager

        database = DownloadDatabase.getInstance(activity)
        adapter = DownloadListAdapter(activity, database, this)
        val decoration = StickyHeaderDecoration(adapter)
        adapter.decoration = decoration
        recyclerView.addItemDecoration(decoration)
        recyclerView.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            return@addCallback if (adapter.isMultiSelectMode) {
                adapter.isMultiSelectMode = false
                true
            } else {
                false
            }
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val activity = activity ?: return

        val info = adapter[position]
        if (info.state == DownloadFileInfo.STATE_DOWNLOADED) {
            info.getFile()?.let {
                try {
                    val filePath = it.uri.resolvePath(activity)
                    val uri = if (filePath != null) {
                        (activity.application as BrowserApplication).providerManager
                                .downloadFileProvider.getUriFromPath(filePath)
                    } else {
                        it.uri
                    }
                    startActivity(createFileOpenIntent(activity, uri, info.mimeType, info.name))
                } catch (e: ActivityNotFoundException) {
                    activity.longToast(R.string.app_notfound)
                }
            }

        }
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int) {
        if (adapter.isMultiSelectMode) {
            adapter.toggle(position)
        } else {
            (activity as AppCompatActivity).startSupportActionMode(this)
            adapter.isMultiSelectMode = true
            adapter.setSelect(position, true)
        }
    }

    override fun onCreateContextMenu(menu: Menu, position: Int) {
        val activity = activity ?: return

        val info = adapter[position]
        val file = info.getFile()

        when (info.state) {
            DownloadFileInfo.STATE_DOWNLOADED -> if (file != null) {
                menu.add(R.string.open_file).setOnMenuItemClickListener {
                    try {
                        val filePath = file.uri.resolvePath(activity)
                        val uri = if (filePath != null) {
                            (activity.application as BrowserApplication).providerManager
                                    .downloadFileProvider.getUriFromPath(filePath)
                        } else {
                            file.uri
                        }
                        startActivity(createFileOpenIntent(activity, uri, info.mimeType, info.name))
                    } catch (e: ActivityNotFoundException) {
                        activity.longToast(R.string.app_notfound)
                    }
                    false
                }
            }
            DownloadFileInfo.STATE_DOWNLOADING -> {
                if (info.resumable) {
                    menu.add(R.string.pause_download).setOnMenuItemClickListener {
                        commandController?.pauseDownload(info.id)
                        false
                    }
                }
                menu.add(R.string.cancel_download).setOnMenuItemClickListener {
                    commandController?.cancelDownload(info.id)
                    false
                }
            }
        }

        if (info.checkFlag(DownloadFileInfo.STATE_PAUSED)) {
            menu.add(R.string.resume_download).setOnMenuItemClickListener {
                activity.reDownload(info.id)
                false
            }
        }

        menu.add(R.string.open_url).setOnMenuItemClickListener {
            startActivity(intentFor(ACTIVITY_MAIN_BROWSER, Intent.EXTRA_TEXT to info.url)
                    .apply { action = Intent.ACTION_VIEW })
            activity.finish()
            false
        }

        if (info.state != DownloadFileInfo.STATE_DOWNLOADING) {
            menu.add(R.string.clear_download).setOnMenuItemClickListener {
                if (database.delete(info.id)) {
                    val index = adapter.indexOf(info)
                    if (index >= 0) {
                        adapter.remove(index)
                    }
                }
                false
            }
        }

        if (info.state == DownloadFileInfo.STATE_DOWNLOADED && file != null) {
            menu.add(R.string.delete_download).setOnMenuItemClickListener {
                if (file.delete()) {
                    database.delete(info.id)
                    val index = adapter.indexOf(info)
                    adapter.remove(index)
                }
                false
            }
        }
    }

    override fun onCancelMultiSelectMode() {
        actionMode?.finish()
    }

    override fun onSelectionStateChange(items: Int) {
        actionMode?.title = getString(R.string.accessibility_toolbar_multi_select, items)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.download_action_mode, menu)
        actionMode = mode
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_bookmark)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val roots = ArrayMap<String, HashMap<String, DocumentFile>>()
                            val selectedItems = adapter.getSelectedItems()
                            selectedItems.forEach {
                                var items = roots[it.root.uri.toString()]
                                if (items == null) {
                                    val files = it.root.listFiles()
                                    items = HashMap(files.size)
                                    files.forEach { file -> file.name?.also { name -> items[name] = file } }
                                    roots[it.root.uri.toString()] = items
                                }
                                items[it.name]?.delete()
                            }
                            database.delete(selectedItems)
                            adapter.reload()

                            adapter.notifyDataSetChanged()

                            mode.finish()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                true
            }
            R.id.deleteFromList -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_bookmark)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            database.delete(adapter.getSelectedItems())
                            adapter.reload()

                            mode.finish()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()

                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        adapter.isMultiSelectMode = false
        actionMode = null
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

    override fun update(info: DownloadFileInfo) {
        adapter.update(info)
    }

    override fun getDownloadInfo(list: List<DownloadFileInfo>) {
        list.forEach(adapter::update)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        commandController = activity as? DownloadCommandController
    }

    override fun onDetach() {
        super.onDetach()
        commandController = null
    }
}
