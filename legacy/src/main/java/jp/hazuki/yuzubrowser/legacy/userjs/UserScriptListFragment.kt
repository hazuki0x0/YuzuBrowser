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

package jp.hazuki.yuzubrowser.legacy.userjs

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.view.filelist.FileListActivity
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_user_script_list.*
import kotlinx.android.synthetic.main.fragment_userjs_item.*
import java.io.File
import java.io.IOException

class UserScriptListFragment : androidx.fragment.app.Fragment(), OnUserJsItemClickListener, DeleteDialogCompat.OnDelete {
    private lateinit var mDb: UserScriptDatabase
    private lateinit var adapter: UserJsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_user_script_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        mDb = UserScriptDatabase.getInstance(activity.applicationContext)

        recyclerView.run {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity))
            val helper = ItemTouchHelper(ListTouch())
            helper.attachToRecyclerView(this)
            addItemDecoration(helper)
        }

        addByEditFab.setOnClickListener {
            startActivityForResult(Intent(activity, UserScriptEditActivity::class.java), REQUEST_ADD_USERJS)
            fabMenu.close(false)
        }

        addFromFileFab.setOnClickListener {
            val intent = Intent(activity, FileListActivity::class.java)
            intent.putExtra(FileListActivity.EXTRA_FILE, Environment.getExternalStorageDirectory())
            startActivityForResult(intent, REQUEST_ADD_FROM_FILE)
            fabMenu.close(false)
        }

        adapter = UserJsAdapter(activity, mDb.allList, this)
        recyclerView.adapter = adapter
    }

    private fun reset() {
        adapter.clear()
        adapter.addAll(mDb.allList)
        adapter.notifyDataSetChanged()
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val script = adapter[position]
        script.isEnabled = !script.isEnabled
        mDb.update(script)
        adapter.notifyItemChanged(position)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        val activity = activity ?: return false

        PopupMenu(activity, v).apply {
            menu.run {
                add(R.string.userjs_info).setOnMenuItemClickListener {
                    onInfoButtonClick(null, position)
                    false
                }

                add(R.string.userjs_edit).setOnMenuItemClickListener {
                    val intent = Intent(activity, UserScriptEditActivity::class.java)
                    val item = adapter[position]
                    intent.putExtra(Intent.EXTRA_TITLE, item.name)
                    intent.putExtra(UserScriptEditActivity.EXTRA_USERSCRIPT, item.id)
                    startActivityForResult(intent, REQUEST_EDIT_USERJS)
                    false
                }

                add(R.string.userjs_delete).setOnMenuItemClickListener {
                    DeleteDialogCompat.newInstance(activity, R.string.confirm, R.string.userjs_delete_confirm, position)
                            .show(childFragmentManager, "delete")
                    false
                }
            }
        }.show()
        return true
    }

    override fun onDelete(position: Int) {
        val js = adapter.remove(position)
        mDb.delete(js)
    }

    override fun onInfoButtonClick(v: View?, index: Int) {
        InfoDialog.newInstance(adapter[index])
                .show(childFragmentManager, "info")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD_USERJS, REQUEST_EDIT_USERJS -> {
                if (resultCode != RESULT_OK) return
                reset()
            }
            REQUEST_ADD_FROM_FILE -> {
                if (resultCode != RESULT_OK || data == null) return
                val file = data.getSerializableExtra(FileListActivity.EXTRA_FILE) as? File ?: throw NullPointerException("file is null")
                AlertDialog.Builder(activity)
                        .setTitle(R.string.confirm)
                        .setMessage(String.format(getString(R.string.userjs_add_file_confirm), file.name))
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            try {
                                val data1 = IOUtils.readFile(file, "UTF-8")
                                mDb.add(UserScript(data1))
                                reset()
                            } catch (e: IOException) {
                                ErrorReport.printAndWriteLog(e)
                                Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            }
        }
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

        override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int =
                ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)

        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            mDb.saveAll(adapter.items)
            return true
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
            val index = viewHolder.adapterPosition
            val js = adapter.remove(index)
            Snackbar.make(linear, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo) {
                        adapter.add(index, js)
                        adapter.notifyDataSetChanged()
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                mDb.delete(js)
                            }
                        }
                    })
                    .show()
        }

        override fun isLongPressDragEnabled(): Boolean = adapter.isSortMode
    }

    class InfoDialog : androidx.fragment.app.DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(activity, R.layout.userjs_info_dialog, null)

            arguments?.let {
                view.findViewById<TextView>(R.id.nameTextView).text = it.getString(NAME)
                view.findViewById<TextView>(R.id.versionTextView).text = it.getString(VERSION)
                view.findViewById<TextView>(R.id.authorTextView).text = it.getString(AUTHOR)
                view.findViewById<TextView>(R.id.descriptionTextView).text = it.getString(DESCRIPTION)
                view.findViewById<TextView>(R.id.includeTextView).text = it.getString(INCLUDE)
                view.findViewById<TextView>(R.id.excludeTextView).text = it.getString(EXCLUDE)
            }

            return AlertDialog.Builder(activity)
                    .setTitle(R.string.userjs_info)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()

        }

        companion object {
            private const val NAME = "name"
            private const val VERSION = "ver"
            private const val AUTHOR = "author"
            private const val DESCRIPTION = "desc"
            private const val INCLUDE = "include"
            private const val EXCLUDE = "exclude"

            fun newInstance(script: UserScript): androidx.fragment.app.DialogFragment {
                return InfoDialog().apply {
                    arguments = Bundle().apply {
                        putString(NAME, script.name)
                        putString(VERSION, script.version)
                        putString(AUTHOR, script.author)
                        putString(DESCRIPTION, script.description)
                        putString(INCLUDE, ArrayUtils.join(script.include, "\n"))
                        putString(EXCLUDE, ArrayUtils.join(script.exclude, "\n"))
                    }
                }
            }
        }
    }

    private class UserJsAdapter internal constructor(context: Context, list: MutableList<UserScript>, private val listener: OnRecyclerListener) : ArrayRecyclerAdapter<UserScript, UserJsAdapter.ViewHolder>(context, list, listener) {

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ViewHolder =
                ViewHolder(inflater.inflate(R.layout.fragment_userjs_item, parent, false), this)

        private fun onInfoButtonClick(v: View, position: Int, js: UserScript) {
            val resolvedPosition = searchPosition(position, js)
            if (resolvedPosition < 0) return
            (listener as OnUserJsItemClickListener)
                    .onInfoButtonClick(v, resolvedPosition)
        }

        internal class ViewHolder(override val containerView: View, adapter: UserJsAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<UserScript>(containerView, adapter), LayoutContainer {
            init {
                infoButton.setOnClickListener { v -> adapter.onInfoButtonClick(v, adapterPosition, item) }
            }

            override fun setUp(item: UserScript) {
                super.setUp(item)
                textView.text = item.name
                checkBox.isChecked = item.isEnabled
            }
        }
    }

    companion object {
        private const val REQUEST_ADD_USERJS = 1
        private const val REQUEST_EDIT_USERJS = 2
        private const val REQUEST_ADD_FROM_FILE = 3
    }
}
