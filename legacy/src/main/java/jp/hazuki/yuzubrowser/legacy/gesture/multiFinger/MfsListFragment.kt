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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureItem
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureManager
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.recycler_with_fab.*

class MfsListFragment : androidx.fragment.app.Fragment(), OnRecyclerListener, DeleteDialogCompat.OnDelete {

    private lateinit var manager: MultiFingerGestureManager
    private lateinit var adapter: MfsListAdapter
    private var listener: OnMfsListListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.recycler_with_fab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        recyclerView.run {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            val helper = ItemTouchHelper(ListTouch())
            helper.attachToRecyclerView(this)
            addItemDecoration(helper)
            addItemDecoration(DividerItemDecoration(activity))
        }

        manager = MultiFingerGestureManager(activity)
        adapter = MfsListAdapter(activity, manager.gestureItems, ActionNameArray(activity), this)

        recyclerView.adapter = adapter

        fab.setOnClickListener {
            listener?.goEdit(-1, MultiFingerGestureItem())
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        if (listener != null)
            listener!!.goEdit(position, adapter.get(position))
    }

    fun onEdited(index: Int, item: MultiFingerGestureItem) {
        if (index >= 0) {
            manager[index] = item
        } else {
            manager.add(item)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        DeleteDialogCompat.newInstance(activity, R.string.delete_mf_gesture, R.string.confirm_delete_mf_gesture, position)
                .show(childFragmentManager, "delete")
        return true
    }

    override fun onDelete(position: Int) {
        manager.remove(position)
        adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is OnMfsListListener)
            listener = activity as OnMfsListListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                val next = !adapter.isSortMode
                adapter.isSortMode = next

                Toast.makeText(activity, if (next) R.string.start_sort else R.string.end_sort, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    private inner class ListTouch : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onMoved(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, fromPos: Int, target: androidx.recyclerview.widget.RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            manager.save()
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val item = adapter.remove(position)

            Snackbar.make(rootLayout, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo) {
                        manager.add(position, item)
                        adapter.notifyDataSetChanged()
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                manager.save()
                            }
                        }
                    })
                    .show()

        }

        override fun isLongPressDragEnabled(): Boolean {
            return adapter.isSortMode
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }
    }

    internal interface OnMfsListListener {
        fun goEdit(index: Int, item: MultiFingerGestureItem)
    }
}
