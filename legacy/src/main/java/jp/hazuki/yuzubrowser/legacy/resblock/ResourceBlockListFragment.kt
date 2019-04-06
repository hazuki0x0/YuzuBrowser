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

package jp.hazuki.yuzubrowser.legacy.resblock

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.resblock.checker.NormalChecker
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.RecyclerFabFragment
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import jp.hazuki.yuzubrowser.ui.widget.recycler.SimpleViewHolder

class ResourceBlockListFragment : RecyclerFabFragment(), OnRecyclerListener, CheckerEditDialog.OnCheckerEdit, DeleteDialogCompat.OnDelete {
    private lateinit var manager: ResourceBlockManager
    private lateinit var adapter: ResBlockAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity ?: return
        val arguments = arguments ?: throw IllegalArgumentException()

        setHasOptionsMenu(true)
        manager = ResourceBlockManager(activity.applicationContext)
        adapter = ResBlockAdapter(activity, manager.list, this)
        setRecyclerViewAdapter(adapter)

        val checker = arguments.getSerializable(CHECKER) as? NormalChecker

        if (checker != null) {
            showEditDialog(-1, checker)
        }
    }

    override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, fromIndex: Int, toIndex: Int): Boolean {
        adapter.move(fromIndex, toIndex)
        return true
    }

    public override fun onMoved(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, fromPos: Int, target: androidx.recyclerview.widget.RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        manager.save(context!!.applicationContext)
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, index: Int) {
        val checker = manager.remove(index)
        adapter.notifyItemRemoved(index)
        val context = context!!.applicationContext
        Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    manager.add(index, checker)
                    adapter.notifyItemInserted(index)
                }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            manager.save(context)
                        }
                    }
                })
                .show()
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        showEditDialog(position, manager.get(position) as NormalChecker)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        DeleteDialogCompat.newInstance(activity, R.string.confirm, R.string.resblock_confirm_delete, position)
                .show(childFragmentManager, "delete")
        return true
    }

    override fun onDelete(position: Int) {
        val activity = activity ?: return

        if (position >= 0 && position < manager.list.size) {
            manager.remove(position)
            manager.save(activity.applicationContext)
            adapter.notifyItemRemoved(position)
        }
    }

    override fun onAddButtonClick() {
        showEditDialog(-1, null)
    }

    private fun showEditDialog(index: Int, checker: NormalChecker?) {
        CheckerEditDialog.newInstance(index, checker)
                .show(childFragmentManager, "edit")
    }

    override fun onCheckerEdited(index: Int, checker: ResourceChecker) {
        if (index >= 0) {
            manager.set(index, checker)
            adapter.notifyItemChanged(index)
        } else {
            manager.add(checker)
            adapter.notifyItemInserted(adapter.itemCount - 1)
        }
        manager.save(context!!.applicationContext)
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

    override val isLongPressDragEnabled: Boolean
        get() = adapter.isSortMode

    private class ResBlockAdapter internal constructor(private val context: Context, list: MutableList<ResourceChecker>, listener: OnRecyclerListener) : ArrayRecyclerAdapter<ResourceChecker, SimpleViewHolder<ResourceChecker>>(context, list, listener) {

        override fun onBindViewHolder(holder: SimpleViewHolder<ResourceChecker>, item: ResourceChecker, position: Int) {
            holder.textView.text = item.getTitle(context)
        }

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): SimpleViewHolder<ResourceChecker> {
            return SimpleViewHolder(
                    inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false),
                    android.R.id.text1,
                    this)
        }
    }

    companion object {
        internal const val CHECKER = "CHECKER"
    }
}
