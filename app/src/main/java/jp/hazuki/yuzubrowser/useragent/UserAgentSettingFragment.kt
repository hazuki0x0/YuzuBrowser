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

package jp.hazuki.yuzubrowser.useragent

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.Toast
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.useragent.SelectActionDialog.DELETE
import jp.hazuki.yuzubrowser.useragent.SelectActionDialog.EDIT
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.recycler_with_fab.*

class UserAgentSettingFragment : Fragment(), DeleteUserAgentDialog.OnDelete, EditUserAgentDialog.OnEditedUserAgent, SelectActionDialog.OnActionSelect, OnRecyclerListener {
    private lateinit var mUserAgentList: UserAgentList
    private lateinit var mAdapter: UserAgentRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.recycler_with_fab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        mUserAgentList = UserAgentList()
        mUserAgentList.read(activity)

        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            val helper = ItemTouchHelper(ListTouch())
            helper.attachToRecyclerView(this)
            addItemDecoration(helper)
            addItemDecoration(DividerItemDecoration(activity))
        }

        mAdapter = UserAgentRecyclerAdapter(activity, mUserAgentList, this)
        recyclerView.adapter = mAdapter

        fab.setOnClickListener { EditUserAgentDialog.newInstance().show(childFragmentManager, "new") }
    }

    override fun onDelete(position: Int) {
        mAdapter.remove(position)
        mUserAgentList.write(activity)
    }

    override fun onEdited(position: Int, name: String, ua: String) {
        if (position < 0) {
            mUserAgentList.add(UserAgent(name, ua))
            mUserAgentList.write(activity)
            mAdapter.notifyItemInserted(mAdapter.size() - 1)
        } else {
            val userAgent = mUserAgentList[position]
            userAgent.name = name
            userAgent.useragent = ua
            mUserAgentList[position] = userAgent
            mUserAgentList.write(activity)
            mAdapter.notifyItemChanged(position)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                val next = !mAdapter.isSortMode
                mAdapter.isSortMode = next

                Toast.makeText(activity, if (next) R.string.start_sort else R.string.end_sort, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    override fun onActionSelected(@SelectActionDialog.ActionMode mode: Int, position: Int, userAgent: UserAgent) {
        when (mode) {
            EDIT -> EditUserAgentDialog.newInstance(position, userAgent)
                    .show(childFragmentManager, "edit")
            DELETE -> DeleteUserAgentDialog.newInstance(position)
                    .show(childFragmentManager, "delete")
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        EditUserAgentDialog.newInstance(position, mUserAgentList[position])
                .show(childFragmentManager, "edit")
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        SelectActionDialog.newInstance(position, mUserAgentList[position])
                .show(childFragmentManager, "action")
        return true
    }

    private inner class ListTouch : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            mAdapter.move(viewHolder.adapterPosition, target.adapterPosition)
            mUserAgentList.write(activity)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val ua = mAdapter.remove(position)

            Snackbar.make(rootLayout, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo) {
                        mAdapter.add(position, ua)
                        mAdapter.notifyItemInserted(position)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && event != Snackbar.Callback.DISMISS_EVENT_MANUAL) {
                                mUserAgentList.write(activity)
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
