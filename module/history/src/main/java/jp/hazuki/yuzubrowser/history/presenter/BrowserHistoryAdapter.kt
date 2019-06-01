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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration
import jp.hazuki.yuzubrowser.core.utility.utils.FontUtils
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryManager
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryModel
import jp.hazuki.yuzubrowser.historyModel.R
import jp.hazuki.yuzubrowser.ui.extensions.decodePunyCodeUrlHost
import jp.hazuki.yuzubrowser.ui.extensions.getColorFromThemeRes
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import java.text.SimpleDateFormat
import java.util.*

class BrowserHistoryAdapter @SuppressLint("SimpleDateFormat")
constructor(
    context: Context,
    private val manager: BrowserHistoryManager,
    private val faviconManager: FaviconManager,
    private val pickMode: Boolean,
    private val listener: OnHistoryRecyclerListener
) : RecyclerView.Adapter<BrowserHistoryAdapter.HistoryHolder>(), StickyHeaderAdapter<BrowserHistoryAdapter.HeaderHolder> {

    private val defaultColorFilter = PorterDuffColorFilter(
        context.getColorFromThemeRes(R.attr.iconColor), PorterDuff.Mode.SRC_ATOP)

    private val dateFormat = DateFormat.getLongDateFormat(context)
    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("kk:mm")
    private var historyModels: MutableList<BrowserHistoryModel> = manager.getList(0, 100)
    private var mQuery: String? = null
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var mDecoration: StickyHeaderDecoration? = null
    private val calendar = Calendar.getInstance()

    var isMultiSelectMode: Boolean = false
        set(multiSelect) {
            if (multiSelect != isMultiSelectMode) {
                field = multiSelect

                if (!multiSelect) {
                    itemSelected.clear()
                    selectedItemCount = 0
                }

                notifyDataSetChanged()
            }
        }

    var selectedItemCount: Int = 0
        private set
    private val itemSelected = SparseBooleanArray()
    private val foregroundOverlay = ColorDrawable(ResourcesCompat.getColor(context.resources,
        R.color.selected_overlay, context.theme))

    val selectedItems: List<Int>
        get() {
            val items = ArrayList<Int>()
            var i = 0
            while (itemSelected.size() > i) {
                if (itemSelected.valueAt(i)) {
                    items.add(itemSelected.keyAt(i))
                }
                i++
            }
            return items
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        return HistoryHolder(inflater.inflate(R.layout.history_item, parent, false))
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = historyModels[holder.adapterPosition]
        val url = item.url?.decodePunyCodeUrlHost()
        val image = faviconManager[item.url!!]

        if (image == null) {
            holder.imageButton.setImageResource(R.drawable.ic_public_white_24dp)
            holder.imageButton.colorFilter = defaultColorFilter
        } else {
            holder.imageButton.setImageBitmap(image)
            holder.imageButton.colorFilter = faviconColorFilter
        }

        if (isMultiSelectMode && isSelected(position)) {
            holder.foreground.background = foregroundOverlay
        } else {
            holder.foreground.background = null
        }
        holder.titleTextView.text = item.title
        holder.urlTextView.text = url
        holder.timeTextView.text = timeFormat.format(Date(item.time))

        holder.itemView.setOnClickListener { v ->
            if (isMultiSelectMode) {
                toggle(holder.adapterPosition)
            } else {
                listener.onRecyclerItemClicked(v, holder.adapterPosition)
            }
        }

        holder.itemView.setOnLongClickListener { v -> listener.onRecyclerItemLongClicked(v, holder.adapterPosition) }

        if (pickMode) {
            holder.imageButton.isClickable = false
            holder.overflowButton.visibility = View.GONE
        } else {
            holder.imageButton.setOnClickListener { v ->
                if (isMultiSelectMode) {
                    toggle(holder.adapterPosition)
                } else {
                    listener.onIconClicked(v, holder.adapterPosition)
                }
            }
            holder.overflowButton.setOnClickListener {
                if (isMultiSelectMode) {
                    toggle(holder.adapterPosition)
                } else {
                    listener.onShowMenu(it, holder.adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return historyModels.size
    }

    fun getItem(position: Int): BrowserHistoryModel {
        return historyModels[position]
    }

    fun remove(position: Int): BrowserHistoryModel {
        return historyModels.removeAt(position)
    }

    fun loadMore() {
        if (mQuery == null) {
            historyModels.addAll(manager.getList(itemCount, 100))
        } else {
            historyModels.addAll(manager.search(mQuery, itemCount, 100))
        }
        mDecoration!!.clearHeaderCache()
    }

    fun reLoad() {
        mQuery = null
        historyModels = manager.getList(0, 100)
        mDecoration!!.clearHeaderCache()
        notifyDataSetChanged()
    }

    fun search(query: String) {
        mQuery = query
        historyModels = manager.search(mQuery, 0, 100)
        mDecoration!!.clearHeaderCache()
        notifyDataSetChanged()
    }

    fun setDecoration(headerDecoration: StickyHeaderDecoration) {
        mDecoration = headerDecoration
    }

    override fun getHeaderId(position: Int): Long {
        val time = historyModels[position].time
        calendar.timeInMillis = time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderHolder {
        return HeaderHolder(inflater.inflate(R.layout.recycler_view_header, parent, false))
    }

    override fun onBindHeaderViewHolder(viewHolder: HeaderHolder, position: Int) {
        viewHolder.header.text = dateFormat.format(Date(historyModels[position].time))
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

    class HistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val foreground: View = itemView.findViewById(R.id.foreground)
        val imageButton: ImageButton = itemView.findViewById(R.id.imageButton)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val urlTextView: TextView = itemView.findViewById(R.id.urlTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val overflowButton: ImageButton = itemView.findViewById(R.id.overflowButton)

        init {
            val fontSizeSetting = AppPrefs.fontSizeHistory.get()
            if (fontSizeSetting >= 0) {
                val normal = FontUtils.getTextSize(fontSizeSetting)
                val small = FontUtils.getSmallerTextSize(fontSizeSetting)

                titleTextView.textSize = normal.toFloat()
                urlTextView.textSize = small.toFloat()
                timeTextView.textSize = small.toFloat()
            }
        }
    }

    class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header: TextView = itemView as TextView

        init {
            val fontSizeSetting = AppPrefs.fontSizeHistory.get()
            if (fontSizeSetting >= 0) {
                header.textSize = FontUtils.getTextSize(fontSizeSetting).toFloat()
            }
        }
    }

    interface OnHistoryRecyclerListener : OnRecyclerListener {
        fun onIconClicked(v: View, position: Int)

        fun onShowMenu(v: View, position: Int)

        fun onSelectionStateChange(items: Int)

        fun onCancelMultiSelectMode()
    }

    companion object {
        private val faviconColorFilter = PorterDuffColorFilter(0, PorterDuff.Mode.SRC_ATOP)
    }
}
