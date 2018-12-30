/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.download.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.hazuki.utility.extensions.intentFor
import jp.hazuki.yuzubrowser.legacy.BrowserActivity
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.legacy.download.core.utils.checkFlag
import jp.hazuki.yuzubrowser.legacy.download.core.utils.getFile
import jp.hazuki.yuzubrowser.legacy.download.reDownload
import jp.hazuki.yuzubrowser.legacy.download.service.DownloadDatabase
import jp.hazuki.yuzubrowser.legacy.download.service.connection.ActivityClient
import jp.hazuki.yuzubrowser.legacy.download.ui.DownloadCommandController
import jp.hazuki.yuzubrowser.legacy.utils.extensions.createFileOpenIntent
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.LoadMoreListener
import org.jetbrains.anko.longToast

class DownloadListFragment : Fragment(), ActivityClient.ActivityClientListener, OnRecyclerMenuListener {

    private var commandController: DownloadCommandController? = null
    private lateinit var adapter: DownloadListAdapter
    private lateinit var database: DownloadDatabase

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
        recyclerView.adapter = adapter
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val activity = activity ?: return

        val info = adapter[position]
        if (info.state == DownloadFileInfo.STATE_DOWNLOADED) {
            info.getFile()?.let {
                try {
                    startActivity(createFileOpenIntent(activity, it.uri, info.mimeType, info.name))
                } catch (e: ActivityNotFoundException) {
                    activity.longToast(R.string.app_notfound)
                }
            }

        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?, position: Int) {
        val activity = activity ?: return

        val info = adapter[position]
        val file = info.getFile()

        when (info.state) {
            DownloadFileInfo.STATE_DOWNLOADED -> if (file != null) {
                menu.add(R.string.open_file).setOnMenuItemClickListener {
                    try {
                        startActivity(createFileOpenIntent(activity, file.uri, info.mimeType, info.name))
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
            startActivity(intentFor<BrowserActivity>(Intent.EXTRA_TEXT to info.url)
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

    override fun update(info: DownloadFileInfo) {
        adapter.update(info)
    }

    override fun getDownloadInfo(list: List<DownloadFileInfo>) {
        list.forEach(adapter::update)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        commandController = activity as? DownloadCommandController
    }

    override fun onDetach() {
        super.onDetach()
        commandController = null
    }
}