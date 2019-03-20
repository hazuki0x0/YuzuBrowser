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

package jp.hazuki.yuzubrowser.legacy.webencode

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.webencode.SelectActionDialog.DELETE
import jp.hazuki.yuzubrowser.legacy.webencode.SelectActionDialog.EDIT
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import javax.inject.Inject

class WebTextEncodeSettingFragment : DaggerFragment(), OnRecyclerListener, EditWebTextEncodeDialog.OnEditedWebTextEncode, SelectActionDialog.OnActionSelect, DeleteDialogCompat.OnDelete {
    private lateinit var mEncodeList: WebTextEncodeList
    private lateinit var mAdapter: WebTextEncodeRecyclerAdapter
    private var rootView: View? = null

    @Inject
    lateinit var moshi: Moshi
    @Inject
    lateinit var applicationContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: throw IllegalStateException()
        rootView = inflater.inflate(R.layout.recycler_with_fab, container, false)
        setHasOptionsMenu(true)

        val recyclerView = rootView!!.findViewById<RecyclerView>(R.id.recyclerView)
        val fab = rootView!!.findViewById<FloatingActionButton>(R.id.fab)

        mEncodeList = WebTextEncodeList()
        mEncodeList.read(activity, moshi)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        val helper = ItemTouchHelper(ListTouch())
        helper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(helper)
        recyclerView.addItemDecoration(DividerItemDecoration(activity))

        mAdapter = WebTextEncodeRecyclerAdapter(activity, mEncodeList, this)
        recyclerView.adapter = mAdapter

        fab.setOnClickListener { EditWebTextEncodeDialog.newInstance().show(childFragmentManager, "new") }
        return rootView
    }

    override fun onDelete(position: Int) {
        mEncodeList.removeAt(position)
        mEncodeList.write(applicationContext, moshi)
        mAdapter.notifyDataSetChanged()
    }

    override fun onEdited(position: Int, name: String) {
        if (position < 0) {
            mEncodeList.add(WebTextEncode(name))
            mEncodeList.write(applicationContext, moshi)
            mAdapter.notifyDataSetChanged()
        } else {
            val encode = mEncodeList[position]
            encode.encoding = name
            mEncodeList[position] = encode
            mEncodeList.write(applicationContext, moshi)
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onActionSelected(@SelectActionDialog.ActionMode mode: Int, position: Int, encode: WebTextEncode) {
        when (mode) {
            EDIT -> EditWebTextEncodeDialog.newInstance(position, encode)
                    .show(childFragmentManager, "edit")
            DELETE -> DeleteDialogCompat.newInstance(activity!!, R.string.delete_web_encode, R.string.delete_web_encode_confirm, position)
                    .show(childFragmentManager, "delete")
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        EditWebTextEncodeDialog.newInstance(position, mEncodeList[position])
                .show(childFragmentManager, "edit")
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        SelectActionDialog.newInstance(position, mEncodeList[position])
                .show(childFragmentManager, "action")
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.sort) {
            val next = !mAdapter.isSortMode
            mAdapter.isSortMode = next

            Toast.makeText(activity, if (next) R.string.start_sort else R.string.end_sort, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private inner class ListTouch : ItemTouchHelper.Callback() {


        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            mAdapter.move(viewHolder.adapterPosition, target.adapterPosition)
            mEncodeList.write(applicationContext, moshi)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val encode = mEncodeList.removeAt(position)

            mAdapter.notifyDataSetChanged()
            Snackbar.make(rootView!!, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo) {
                        mEncodeList.add(position, encode)
                        mAdapter.notifyDataSetChanged()
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                mEncodeList.write(applicationContext, moshi)
                            }
                        }
                    })
                    .show()
        }

        override fun isLongPressDragEnabled(): Boolean {
            return mAdapter.isSortMode
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }
    }
}
