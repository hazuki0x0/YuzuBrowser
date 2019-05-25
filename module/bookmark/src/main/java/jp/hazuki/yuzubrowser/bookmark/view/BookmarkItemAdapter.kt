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

package jp.hazuki.yuzubrowser.bookmark.view

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import jp.hazuki.bookmark.R
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkItem
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkSite
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.core.utility.utils.FontUtils
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.ui.extensions.getColorFromThemeRes
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

open class BookmarkItemAdapter(
    protected val context: Context,
    list: MutableList<BookmarkItem>,
    private val pickMode: Boolean,
    private val openNewTab: Boolean,
    protected val fontSize: Int,
    private val faviconManager: FaviconManager,
    private val bookmarkItemListener: OnBookmarkRecyclerListener
) : ArrayRecyclerAdapter<BookmarkItem, BookmarkItemAdapter.BookmarkFolderHolder>(context, list, null) {

    private val defaultColorFilter: PorterDuffColorFilter =
        PorterDuffColorFilter(context.getColorFromThemeRes(R.attr.iconColor), PorterDuff.Mode.SRC_ATOP)

    private val foregroundOverlay =
        ColorDrawable(context.getResColor(R.color.selected_overlay))

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
            holder as SimpleBookmarkSiteHolder
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

    protected fun onIconClick(v: View, position: Int, item: BookmarkItem) {
        val calPosition = searchPosition(position, item)
        if (calPosition < 0) return
        bookmarkItemListener.onIconClick(v, calPosition)
    }

    protected fun onOverflowButtonClick(v: View, position: Int, item: BookmarkItem) {
        val calPosition = searchPosition(position, item)
        if (calPosition < 0) return
        bookmarkItemListener.onShowMenu(v, calPosition)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): BookmarkFolderHolder {
        return when (viewType) {
            TYPE_SITE -> SimpleBookmarkSiteHolder(inflater.inflate(R.layout.bookmark_item_site, parent, false), this)
            TYPE_FOLDER -> BookmarkFolderHolder(inflater.inflate(R.layout.bookmark_item_folder, parent, false), this)
            else -> throw IllegalStateException("Unknown BookmarkItem type")
        }
    }

    fun getFavicon(site: BookmarkSite): ByteArray? {
        return faviconManager.getFaviconBytes(site.url)
    }

    override fun setSelect(position: Int, isSelect: Boolean) {
        super.setSelect(position, isSelect)
        if (selectedItemCount == 0) {
            bookmarkItemListener.onCancelMultiSelectMode()
        } else {
            bookmarkItemListener.onSelectionStateChange(selectedItemCount)
        }
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
            val fontSize = adapter.fontSize
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

    open class SimpleBookmarkSiteHolder(itemView: View, adapter: BookmarkItemAdapter) : BookmarkFolderHolder(itemView, adapter) {
        val imageButton: ImageButton = itemView.findViewById(R.id.imageButton)

        init {
            imageButton.setOnClickListener {
                if (adapter.isMultiSelectMode) {
                    adapter.toggle(adapterPosition)
                } else {
                    adapter.onIconClick(it, adapterPosition, item)
                }
            }
        }
    }

    interface OnBookmarkRecyclerListener : OnRecyclerListener {
        fun onIconClick(v: View, position: Int)

        fun onShowMenu(v: View, position: Int)

        fun onSelectionStateChange(items: Int)

        fun onCancelMultiSelectMode()
    }

    companion object {
        const val TYPE_SITE = 1
        const val TYPE_FOLDER = 2
    }
}
