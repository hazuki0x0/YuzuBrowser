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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.format.DateFormat
import android.text.format.Formatter
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.util.forEach
import androidx.recyclerview.widget.RecyclerView
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration
import jp.hazuki.yuzubrowser.core.utility.extensions.binarySearchLong
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.download.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.utils.getNotificationString
import jp.hazuki.yuzubrowser.download.service.DownloadDatabase
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_download_list_item.*
import java.text.SimpleDateFormat
import java.util.*

class DownloadListAdapter(
        private val context: Context,
        private val database: DownloadDatabase,
        private val listener: OnRecyclerMenuListener
) : RecyclerView.Adapter<DownloadListAdapter.InfoHolder>(),
        StickyHeaderAdapter<DownloadListAdapter.HeaderHolder> {

    private val items = ArrayList<DownloadFileInfo>(database.getList(0, 100))
    private val inflater = LayoutInflater.from(context)
    private val calendar = Calendar.getInstance()
    private val dateFormat = DateFormat.getLongDateFormat(context)
    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("kk:mm")

    var decoration: StickyHeaderDecoration? = null

    var selectedItemCount: Int = 0
        private set

    private val itemSelected = SparseBooleanArray()
    private val foregroundOverlay = ColorDrawable(context.getResColor(R.color.selected_overlay))
    var isMultiSelectMode = false
        set(value) {
            if (value != field) {
                field = value

                if (!value) {
                    itemSelected.clear()
                    selectedItemCount = 0
                }

                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        return InfoHolder(inflater.inflate(R.layout.fragment_download_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.size > 0) update(holder, position, payloads)
        else onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        val item = items[position]

        holder.filenameTextView.text = item.name
        holder.urlTextView.text = item.url
        holder.timeTextView.text = timeFormat.format(Date(item.startTime))
        holder.foreground.background =
                if (isMultiSelectMode && isSelected(position)) foregroundOverlay else null

        holder.urlTextView.text = if (item.url.startsWith("data:")) {
            var end = item.url.indexOf(';')
            if (end < 0) {
                end = item.url.indexOf(',')
            }
            item.url.substring(5, end)
        } else {
            Uri.parse(item.url).host
        }

        updateState(holder, item)

        holder.itemView.setOnClickListener {
            if (isMultiSelectMode) {
                toggle(holder.adapterPosition)
            } else {
                listener.onRecyclerItemClicked(it, holder.adapterPosition)
            }
        }
        holder.overflowButton.setOnClickListener {
            if (isMultiSelectMode) {
                toggle(holder.adapterPosition)
            } else {
                val popupMenu = PopupMenu(context, it)
                listener.onCreateContextMenu(popupMenu.menu, holder.adapterPosition)
                popupMenu.show()
            }
        }
        holder.itemView.setOnLongClickListener {
            listener.onRecyclerItemLongClicked(it, holder.adapterPosition)
            true
        }
    }

    private fun update(holder: InfoHolder, position: Int, payloads: MutableList<Any>) {
        val payload = payloads[0] as? String
        when (payload) {
            PAYLOAD_UPDATE_STATE -> {
                updateState(holder, items[position])
            }
        }
    }

    private fun updateState(holder: InfoHolder, info: DownloadFileInfo) {
        when (info.state) {
            DownloadFileInfo.STATE_DOWNLOADED -> {
                holder.statusTextView.setText(R.string.download_success)
                if (info.size < 0) {
                    holder.sizeTextView.setText(R.string.unknown)
                } else {
                    holder.sizeTextView.text = Formatter.formatFileSize(context, info.size)
                }
                holder.sizeTextView.visibility = View.VISIBLE
                holder.splitTextView.visibility = View.VISIBLE
                holder.progressBar.visibility = View.GONE
            }
            DownloadFileInfo.STATE_CANCELED -> {
                holder.statusTextView.setText(R.string.download_cancel)
                holder.sizeTextView.visibility = View.GONE
                holder.splitTextView.visibility = View.GONE
                holder.progressBar.visibility = View.GONE
            }
            DownloadFileInfo.STATE_DOWNLOADING -> {
                holder.statusTextView.text = info.getNotificationString(context)
                holder.sizeTextView.visibility = View.GONE
                holder.splitTextView.visibility = View.GONE

                holder.progressBar.run {
                    visibility = View.VISIBLE
                    progress = info.currentSize.toInt()
                    max = info.size.toInt()
                    isIndeterminate = info.size <= 0
                }
            }
            DownloadFileInfo.STATE_PAUSED, DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED -> {
                holder.statusTextView.setText(R.string.download_paused)
                holder.sizeTextView.visibility = View.GONE
                holder.splitTextView.visibility = View.GONE
                holder.progressBar.visibility = View.GONE
            }
            else -> {
                holder.statusTextView.setText(R.string.download_fail)
                holder.sizeTextView.visibility = View.GONE
                holder.splitTextView.visibility = View.GONE
                holder.progressBar.visibility = View.GONE
            }
        }
    }

    fun update(info: DownloadFileInfo) {
        val index = indexOf(info)
        if (index >= 0) {
            items[index] = info
            notifyItemChanged(index, PAYLOAD_UPDATE_STATE)
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
        return items.binarySearchLong { info.id - it.id }
    }

    fun reload() {
        items.clear()
        items.addAll(database.getList(itemCount, 100))
        decoration?.clearHeaderCache()
        notifyDataSetChanged()
    }

    fun loadMore() {
        items.addAll(database.getList(itemCount, 100))
        decoration?.clearHeaderCache()
        notifyDataSetChanged()
    }

    fun toggle(position: Int) {
        setSelect(position, !itemSelected.get(position, false))
    }

    fun setSelect(position: Int, isSelect: Boolean) {
        val old = itemSelected.get(position, false)
        itemSelected.put(position, isSelect)

        if (old != isSelect) {
            notifyItemChanged(position)
            if (isSelect) selectedItemCount++ else selectedItemCount--
            if (selectedItemCount == 0) {
                listener.onCancelMultiSelectMode()
            } else {
                listener.onSelectionStateChange(selectedItemCount)
            }
        }
    }

    fun isSelected(position: Int): Boolean {
        return itemSelected.get(position, false)
    }

    fun getSelectedItems(): List<DownloadFileInfo> {
        val selected = ArrayList<DownloadFileInfo>()
        itemSelected.forEach { key, value ->
            if (value) selected.add(items[key])
        }
        return selected
    }

    override fun getItemCount() = items.size

    class InfoHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    override fun getHeaderId(position: Int): Long {
        calendar.timeInMillis = items[position].startTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderHolder {
        return HeaderHolder(inflater.inflate(R.layout.recycler_view_header, parent, false))
    }

    override fun onBindHeaderViewHolder(viewholder: HeaderHolder, position: Int) {
        viewholder.header.text = dateFormat.format(Date(items[position].startTime))
    }

    class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: TextView = itemView as TextView
    }

    companion object {
        const val PAYLOAD_UPDATE_STATE = "update_state"
    }
}