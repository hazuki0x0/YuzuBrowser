/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.utils.view.recycler

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.hazuki.yuzubrowser.R
import kotlinx.android.synthetic.main.recycler_with_fab.*

abstract class RecyclerFabFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_with_fab, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        fab.run {
            setOnClickListener { onAddButtonClick() }
            setOnLongClickListener { onAddButtonLongClick() }
        }
        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            val helper = ItemTouchHelper(ListTouch())
            helper.attachToRecyclerView(this)
            addItemDecoration(helper)
            addItemDecoration(DividerItemDecoration(activity))
        }
    }

    protected fun setRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter = adapter
    }

    protected val rootView: View
        get() = rootLayout

    protected open fun onAddButtonClick() {}

    protected open fun onAddButtonLongClick() = false

    abstract fun onMove(recyclerView: RecyclerView, fromIndex: Int, toIndex: Int): Boolean

    protected open fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {}

    abstract fun onSwiped(viewHolder: RecyclerView.ViewHolder, index: Int)

    open val isLongPressDragEnabled: Boolean = true

    open val isItemViewSwipeEnabled: Boolean = true

    fun setAddButtonEnabled(enabled: Boolean) {
        fab.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private inner class ListTouch : ItemTouchHelper.Callback() {


        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return this@RecyclerFabFragment.onMove(recyclerView, viewHolder.adapterPosition, target.adapterPosition)
        }

        override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            this@RecyclerFabFragment.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            this@RecyclerFabFragment.onSwiped(viewHolder, viewHolder.adapterPosition)
        }

        override fun isLongPressDragEnabled(): Boolean {
            return this@RecyclerFabFragment.isLongPressDragEnabled
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return this@RecyclerFabFragment.isItemViewSwipeEnabled
        }
    }
}