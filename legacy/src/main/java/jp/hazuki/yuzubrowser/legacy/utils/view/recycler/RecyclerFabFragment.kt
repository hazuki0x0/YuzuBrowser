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

package jp.hazuki.yuzubrowser.legacy.utils.view.recycler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import jp.hazuki.yuzubrowser.legacy.databinding.RecyclerWithFabBinding
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration

abstract class RecyclerFabFragment : Fragment() {

    private var viewBinding: RecyclerWithFabBinding? = null

    private val binding: RecyclerWithFabBinding
        get() = viewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        viewBinding = RecyclerWithFabBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        binding.apply {
            fab.run {
                setOnClickListener { onAddButtonClick() }
                setOnLongClickListener { onAddButtonLongClick() }
            }
            recyclerView.run {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
                val helper = ItemTouchHelper(ListTouch())
                helper.attachToRecyclerView(this)
                addItemDecoration(helper)
                if (isNeedDivider) {
                    addItemDecoration(DividerItemDecoration(activity))
                }
            }
        }
    }

    protected fun setRecyclerViewAdapter(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>) {
        binding.recyclerView.adapter = adapter
    }

    protected val rootView: View
        get() = binding.rootLayout

    protected open fun onAddButtonClick() {}

    protected open fun onAddButtonLongClick() = false

    protected open val isNeedDivider: Boolean
        get() = true

    abstract fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, fromIndex: Int, toIndex: Int): Boolean

    protected open fun onMoved(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, fromPos: Int, target: androidx.recyclerview.widget.RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {}

    abstract fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, index: Int)

    open val isLongPressDragEnabled: Boolean = true

    open val isItemViewSwipeEnabled: Boolean = true

    fun setAddButtonEnabled(enabled: Boolean) {
        binding.fab.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private inner class ListTouch : ItemTouchHelper.Callback() {


        override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            return this@RecyclerFabFragment.onMove(recyclerView, viewHolder.adapterPosition, target.adapterPosition)
        }

        override fun onMoved(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, fromPos: Int, target: androidx.recyclerview.widget.RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            this@RecyclerFabFragment.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
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
