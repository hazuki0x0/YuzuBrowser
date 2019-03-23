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

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import jp.hazuki.yuzubrowser.core.utility.utils.FontUtils
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkSite
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.utils.ThemeUtils
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class BookmarkItemAdapter(private val context: Context, list: MutableList<BookmarkItem>, private val pickMode: Boolean, private val openNewTab: Boolean, private val bookmarkItemListener: OnBookmarkRecyclerListener) : ArrayRecyclerAdapter<BookmarkItem, BookmarkItemAdapter.BookmarkFolderHolder>(context, list, null) {

    private val defaultColorFilter: PorterDuffColorFilter =
            PorterDuffColorFilter(ThemeUtils.getColorFromThemeRes(context, R.attr.iconColor), PorterDuff.Mode.SRC_ATOP)

    private val foregroundOverlay =
            ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.selected_overlay, context.theme))
    private val faviconManager: FaviconManager = FaviconManager.getInstance(context)

    init {
        setRecyclerListener(object : OnRecyclerListener {
            override fun onRecyclerItemClicked(v: View, position: Int) {
                if (isMultiSelectMode) {
                    toggle(position)
                } else {
                    bookmarkItemListener.onRecyclerItemClicked(v, position)
                }
            }

            override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
                return bookmarkItemListener.onRecyclerItemLongClicked(v, position)
            }
        })
    }

    override fun onBindViewHolder(holder: BookmarkFolderHolder, item: BookmarkItem, position: Int) {
        if (item is BookmarkSite) {
            var host: String? = if (item.url.startsWith("javascript:"))
                context.getString(R.string.bookmarklet)
            else
                item.url.toUri().host

            if (host.isNullOrEmpty()) host = item.url
            (holder as BookmarkSiteHolder).url.text = host
            if (!openNewTab || pickMode || isMultiSelectMode) {
                holder.imageButton.isEnabled = false
                holder.imageButton.isClickable = false
            } else {
                holder.imageButton.isEnabled = true
                holder.imageButton.isClickable = true
            }

            val bitmap = faviconManager[item.url]
            if (bitmap != null) {
                holder.imageButton.setImageBitmap(bitmap)
                holder.imageButton.clearColorFilter()
            } else {
                holder.imageButton.setImageResource(R.drawable.ic_bookmark_white_24dp)
                holder.imageButton.colorFilter = defaultColorFilter
            }
        }

        if (isMultiSelectMode && isSelected(position)) {
            holder.foreground.background = foregroundOverlay
        } else {
            holder.foreground.background = null
        }
    }

    private fun onIconClick(v: View, position: Int, item: BookmarkItem) {
        val calPosition = searchPosition(position, item)
        if (calPosition < 0) return
        bookmarkItemListener.onIconClick(v, calPosition)
    }

    private fun onOverflowButtonClick(v: View, position: Int, item: BookmarkItem) {
        val calPosition = searchPosition(position, item)
        if (calPosition < 0) return
        bookmarkItemListener.onShowMenu(v, calPosition)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): BookmarkFolderHolder {
        return when (viewType) {
            TYPE_SITE -> BookmarkSiteHolder(inflater.inflate(R.layout.bookmark_item_site, parent, false), this)
            TYPE_FOLDER -> BookmarkFolderHolder(inflater.inflate(R.layout.bookmark_item_folder, parent, false), this)
            else -> throw IllegalStateException("Unknown BookmarkItem type")
        }
    }

    fun getFavicon(site: BookmarkSite): ByteArray? {
        return faviconManager.getFaviconBytes(site.url)
    }

    override fun setSelect(position: Int, isSelect: Boolean) {
        super.setSelect(position, isSelect)
        if (selectedItemCount == 0) bookmarkItemListener.onCancelMultiSelectMode()
    }

    override fun getItemViewType(position: Int): Int {
        val item = get(position)
        return when (item) {
            is BookmarkSite -> TYPE_SITE
            is BookmarkFolder -> TYPE_FOLDER
            else -> throw IllegalStateException("Unknown BookmarkItem type")
        }
    }

    open class BookmarkFolderHolder(itemView: View, adapter: BookmarkItemAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<BookmarkItem>(itemView, adapter) {
        val title: TextView = itemView.findViewById(R.id.titleTextView)
        val more: ImageButton = itemView.findViewById(R.id.overflowButton)
        val foreground: View = itemView.findViewById(R.id.foreground)


        init {
            val fontSize = AppData.font_size.bookmark.get()
            if (fontSize >= 0) {
                title.textSize = FontUtils.getTextSize(fontSize).toFloat()
            }

            more.setOnClickListener {
                if (adapter.isMultiSelectMode) {
                    adapter.toggle(adapterPosition)
                } else {
                    adapter.onOverflowButtonClick(more, adapterPosition, item)
                }
            }
        }

        override fun setUp(item: BookmarkItem) {
            super.setUp(item)
            title.text = item.title
        }
    }

    class BookmarkSiteHolder(itemView: View, adapter: BookmarkItemAdapter) : BookmarkFolderHolder(itemView, adapter) {
        val imageButton: ImageButton = itemView.findViewById(R.id.imageButton)
        val url: TextView = itemView.findViewById(R.id.urlTextView)

        init {
            imageButton.setOnClickListener {
                if (adapter.isMultiSelectMode) {
                    adapter.toggle(adapterPosition)
                } else {
                    adapter.onIconClick(it, adapterPosition, item)
                }
            }

            val fontSize = AppData.font_size.bookmark.get()
            if (fontSize >= 0) {
                url.textSize = FontUtils.getSmallerTextSize(fontSize).toFloat()
            }
        }
    }

    interface OnBookmarkRecyclerListener : OnRecyclerListener {
        fun onIconClick(v: View, position: Int)

        fun onShowMenu(v: View, position: Int)

        fun onCancelMultiSelectMode()
    }

    companion object {
        private const val TYPE_SITE = 1
        private const val TYPE_FOLDER = 2
    }
}
