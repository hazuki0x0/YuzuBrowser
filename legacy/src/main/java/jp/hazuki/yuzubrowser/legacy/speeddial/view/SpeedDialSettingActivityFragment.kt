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

package jp.hazuki.yuzubrowser.legacy.speeddial.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDial
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialManager
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.recycler_with_fab.*
import java.util.*


class SpeedDialSettingActivityFragment : androidx.fragment.app.Fragment(), OnRecyclerListener, FabActionCallBack, SpeedDialEditCallBack, DeleteDialogCompat.OnDelete {

    private lateinit var manager: SpeedDialManager
    private lateinit var speedDialList: ArrayList<SpeedDial>
    private lateinit var adapter: SpeedDialRecyclerAdapter
    private var mListener: OnSpeedDialAddListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.recycler_with_fab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        manager = SpeedDialManager(activity.applicationContext)
        speedDialList = manager.all

        recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            val helper = ItemTouchHelper(ListTouch())
            helper.attachToRecyclerView(this)
            addItemDecoration(helper)
            addItemDecoration(DividerItemDecoration(activity))
        }

        adapter = SpeedDialRecyclerAdapter(activity, speedDialList, this)
        recyclerView.adapter = adapter

        fab.setOnClickListener { FabActionDialog().show(childFragmentManager, "fab") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
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

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val speedDial = speedDialList[position]
        mListener?.goEdit(speedDial)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        DeleteDialogCompat.newInstance(activity, R.string.delete_speedDial, R.string.confirm_delete_speedDial, position)
                .show(childFragmentManager, "delete")
        return true
    }

    override fun onDelete(position: Int) {
        val (id) = speedDialList.removeAt(position)
        adapter.notifyDataSetChanged()
        manager.delete(id)
    }


    override fun onAdd(which: Int) {
        mListener?.run {
            when (which) {
                0 -> goEdit(SpeedDial())
                1 -> addFromBookmark()
                2 -> addFromHistory()
                3 -> addFromAppList()
                4 -> addFromShortCutList()
            }
        }
    }

    override fun onEdited(speedDial: SpeedDial) {
        val index = speedDialList.indexOf(speedDial)
        if (index >= 0) {
            speedDialList[index] = speedDial
        } else {
            speedDialList.add(speedDial)
        }

        manager.update(speedDial)
        adapter.notifyDataSetChanged()
    }

    class FabActionDialog : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.new_speed_dial)
                    .setItems(R.array.new_speed_dial_mode) { _, which ->
                        if (parentFragment is FabActionCallBack) {
                            (parentFragment as FabActionCallBack).onAdd(which)
                        }
                        dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
            return builder.create()
        }


    }

    private inner class ListTouch : ItemTouchHelper.Callback() {


        override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            manager.updateOrder(speedDialList)
            return true
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val speedDial = speedDialList.removeAt(position)

            adapter.notifyDataSetChanged()
            Snackbar.make(rootLayout, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo) {
                        speedDialList.add(position, speedDial)
                        adapter.notifyDataSetChanged()
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                manager.delete(speedDial.id)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = activity as OnSpeedDialAddListener
        } catch (e: ClassCastException) {
            throw IllegalStateException(e)
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnSpeedDialAddListener {
        fun goEdit(speedDial: SpeedDial)

        fun addFromBookmark()

        fun addFromHistory()

        fun addFromAppList()

        fun addFromShortCutList()
    }
}
