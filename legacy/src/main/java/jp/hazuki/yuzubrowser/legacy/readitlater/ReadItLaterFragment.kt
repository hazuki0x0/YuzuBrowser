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

package jp.hazuki.yuzubrowser.legacy.readitlater

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.TextView
import jp.hazuki.yuzubrowser.core.utility.extensions.intentFor
import jp.hazuki.yuzubrowser.core.utility.utils.FontUtils
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import jp.hazuki.yuzubrowser.ui.extensions.decodePunyCodeUrlHost
import jp.hazuki.yuzubrowser.ui.provider.IReadItLaterProvider
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import java.text.SimpleDateFormat
import java.util.*

class ReadItLaterFragment : androidx.fragment.app.Fragment(), OnRecyclerListener, ActionMode.Callback {

    private lateinit var adapter: ReaderAdapter
    private lateinit var readItLaterProvider: IReadItLaterProvider

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        readItLaterProvider = (activity.applicationContext as BrowserApplication).providerManager.readItLaterProvider

        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(activity))

        adapter = ReaderAdapter(activity, getList(), this)
        recyclerView.adapter = adapter
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val activity = activity ?: return

        startActivity(intentFor(Constants.activity.MAIN_BROWSER).apply {
            action = Constants.intent.ACTION_OPEN_DEFAULT
            data = readItLaterProvider.getReadUri(adapter[position].time)
        })
        activity.finish()
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        val activity = activity ?: return false

        activity.startActionMode(this)
        adapter.isMultiSelectMode = true
        adapter.setSelect(position, true)
        return true
    }

    override fun onActionItemClicked(p0: ActionMode, menu: MenuItem): Boolean {
        val activity = activity ?: return false

        return when (menu.itemId) {
            R.id.delete -> {
                val items = adapter.selectedItems
                val resolver = activity.contentResolver
                items.reversed()
                        .map { adapter.remove(it) }
                        .forEach { resolver.delete(readItLaterProvider.getEditUri(it.time), null, null) }

                adapter.notifyDataSetChanged()
                p0.finish()
                return true
            }
            else -> false
        }
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        actionMode.menuInflater.inflate(R.menu.action_delete, menu)
        return true
    }

    override fun onPrepareActionMode(p0: ActionMode?, menu: Menu?): Boolean = false

    override fun onDestroyActionMode(p0: ActionMode?) {
        adapter.isMultiSelectMode = false
    }

    private fun getList(): MutableList<ReadItem> {
        val activity = activity ?: throw IllegalStateException()

        activity.contentResolver.query(readItLaterProvider.editUri, null, null, null, null).use { cursor ->
            checkNotNull(cursor)
            val list = ArrayList<ReadItem>()
            while (cursor.moveToNext()) {
                list.add(ReadItem(
                        cursor.getLong(IReadItLaterProvider.COL_TIME),
                        cursor.getString(IReadItLaterProvider.COL_URL),
                        cursor.getString(IReadItLaterProvider.COL_TITLE)))
            }
            return list
        }

    }

    private class ReaderAdapter(context: Context, list: MutableList<ReadItem>, listener: OnRecyclerListener)
        : ArrayRecyclerAdapter<ReadItem, ReaderAdapter.ReaderViewHolder>(context, list, listener) {
        val date = DateFormat.getDateFormat(context)!!
        @SuppressLint("SimpleDateFormat")
        val time = SimpleDateFormat("kk:mm")

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ReaderViewHolder {
            return ReaderViewHolder(inflater.inflate(R.layout.read_it_later_item, parent, false), this)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ReaderViewHolder, item: ReadItem, position: Int) {
            val d = Date(item.time)
            holder.time.text = date.format(d) + " " + time.format(d)

            holder.foreground.visibility = if (isMultiSelectMode && isSelected(position)) View.VISIBLE else View.INVISIBLE
        }

        private class ReaderViewHolder(view: View, adapter: ReaderAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<ReadItem>(view, adapter) {
            val title = view.findViewById<TextView>(R.id.titleTextView)!!
            val url = view.findViewById<TextView>(R.id.urlTextView)!!
            val time = view.findViewById<TextView>(R.id.timeTextView)!!
            val foreground = view.findViewById<View>(R.id.foreground)!!

            init {
                val font = AppPrefs.font_size.readItLater.get()
                if (font >= 0) {
                    title.textSize = FontUtils.getTextSize(font).toFloat()
                    val small = FontUtils.getSmallerTextSize(font).toFloat()
                    url.textSize = small
                    time.textSize = small
                }
            }

            override fun setUp(item: ReadItem) {
                super.setUp(item)
                title.text = item.title
                url.text = item.url.decodePunyCodeUrlHost()
            }
        }
    }
}
