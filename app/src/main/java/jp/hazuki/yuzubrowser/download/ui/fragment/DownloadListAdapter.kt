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

package jp.hazuki.yuzubrowser.download.ui.fragment

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.utils.getNotificationString
import jp.hazuki.yuzubrowser.download.service.DownloadDatabase
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.download_list_item.*

class DownloadListAdapter(private val context: Context, private val database: DownloadDatabase, private val listener: OnRecyclerMenuListener) : RecyclerView.Adapter<DownloadListAdapter.InfoHolder>() {
    private val items = ArrayList<DownloadFileInfo>(database.getList(0, 100))
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): InfoHolder {
        return InfoHolder(inflater.inflate(R.layout.fragment_download_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        val item = items[position]
        item.root.exists()

        holder.filenameTextView.text = item.name
        holder.urlTextView.text = item.url

        when (item.state) {
            DownloadFileInfo.STATE_DOWNLOADED -> {
                holder.statusTextView.setText(R.string.download_success)
                holder.progressBar.visibility = View.GONE
            }
            DownloadFileInfo.STATE_CANCELED -> {
                holder.statusTextView.setText(R.string.download_cancel)
                holder.progressBar.visibility = View.GONE
            }
            DownloadFileInfo.STATE_DOWNLOADING -> {
                holder.statusTextView.text = item.getNotificationString(context)

                holder.progressBar.run {
                    visibility = View.VISIBLE
                    progress = item.currentSize.toInt()
                    max = item.size.toInt()
                    isIndeterminate = item.size <= 0
                }
            }
            DownloadFileInfo.STATE_PAUSED, DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED -> {
                holder.statusTextView.setText(R.string.download_paused)
                holder.progressBar.visibility = View.GONE
            }
            else -> {
                holder.statusTextView.setText(R.string.download_fail)
                holder.progressBar.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener { listener.onRecyclerItemClicked(it, holder.adapterPosition) }
        holder.itemView.setOnCreateContextMenuListener { menu, v, menuInfo -> listener.onCreateContextMenu(menu, v, menuInfo, holder.adapterPosition) }
    }

    fun update(info: DownloadFileInfo) {
        val index = indexOf(info)
        if (index >= 0) {
            items[index] = info
            notifyItemChanged(index)
        }
    }

    operator fun get(position: Int) = items[position]

    fun remove(position: Int) {
        if (position >= 0) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun remove(info: DownloadFileInfo) = remove(indexOf(info))

    fun indexOf(info: DownloadFileInfo): Int {
        items.forEachIndexed { index, item ->
            if (item.id == info.id) return index
        }
        return -1
    }

    fun loadMore() {
        items.addAll(database.getList(itemCount, 100))
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    class InfoHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}