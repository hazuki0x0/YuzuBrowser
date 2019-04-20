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

package jp.hazuki.yuzubrowser.adblock.ui.original

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlock
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlockManager
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.fragment_ad_block_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.PrintWriter
import java.util.*

class AdBlockFragment : Fragment(), OnRecyclerListener, AdBlockEditDialog.AdBlockEditDialogListener, AdBlockMenuDialog.OnAdBlockMenuListener, AdBlockItemDeleteDialog.OnBlockItemDeleteListener, ActionMode.Callback, DeleteSelectedDialog.OnDeleteSelectedListener, AdBlockDeleteAllDialog.OnDeleteAllListener {

    private lateinit var provider: AdBlockManager.AdBlockItemProvider
    private lateinit var adapter: AdBlockArrayRecyclerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var listener: AdBlockFragmentListener? = null
    private var actionMode: ActionMode? = null
    private var type: Int = 0
    private var isModified = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_ad_block_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return
        val arguments = arguments ?: return
        type = arguments.getInt(ARG_TYPE)
        listener!!.setFragmentTitle(type)
        provider = AdBlockManager.getProvider(activity.applicationContext, type)
        adapter = AdBlockArrayRecyclerAdapter(activity, provider.allItems, this)
        layoutManager = LinearLayoutManager(activity)

        recyclerView.let {
            it.layoutManager = layoutManager
            it.addItemDecoration(DividerItemDecoration(activity))
            it.adapter = adapter
        }

        addByEditFab.setOnClickListener {
            AdBlockEditDialog(getString(R.string.add))
                    .show(childFragmentManager, "add")
            fabMenu.close(true)
        }

        addFromFileFab.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, null), REQUEST_SELECT_FILE)
            fabMenu.close(false)
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        isModified = true
        val adBlock = adapter[position]
        adBlock.isEnable = !adBlock.isEnable
        provider.update(adBlock)
        adapter.notifyItemChanged(position)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        if (!adapter.isMultiSelectMode) {
            AdBlockMenuDialog(position, adapter[position].id)
                    .show(childFragmentManager, "menu")
        }
        return true
    }

    override fun onEdited(index: Int, id: Int, text: String) {
        isModified = true
        if (index >= 0 && index < adapter.itemCount) {
            val adBlock = adapter[index]
            adBlock.match = text
            if (provider.update(adBlock)) {
                adapter.notifyItemChanged(index)
            } else {
                adapter.remove(index)
                provider.delete(adBlock.id)
            }
        } else {
            if (id > -1) {
                var i = 0
                while (adapter.size() > i) {
                    val adBlock = adapter[i]
                    if (adBlock.id == id) {
                        adBlock.match = text
                        if (provider.update(adBlock)) {
                            adapter.notifyItemChanged(i)
                        } else {
                            adapter.remove(i)
                            provider.delete(adBlock.id)
                        }
                        break
                    }
                    i++
                }
            } else {
                val adBlock = AdBlock(text)
                if (provider.update(adBlock)) {
                    adapter.add(adBlock)
                    adapter.notifyDataSetChanged()
                }

            }
        }
    }

    fun addAll(adBlocks: List<AdBlock>) {
        isModified = true
        provider.addAll(adBlocks)
        adapter.clear()
        adapter.addAll(provider.allItems)
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_FILE -> if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                listener!!.requestImport(data.data!!)
            }
            REQUEST_SELECT_EXPORT -> if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                val handler = Handler()
                Thread(Runnable {
                    try {
                        context?.run {
                            contentResolver.openOutputStream(data.data!!)!!.use { os ->
                                PrintWriter(os).use { pw ->
                                    val adBlockList = provider.enableItems
                                    for ((_, match) in adBlockList)
                                        pw.println(match)
                                    handler.post { Toast.makeText(context, R.string.pref_exported, Toast.LENGTH_SHORT).show() }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }).run()
            }
        }
    }

    override fun onAskDelete(index: Int, id: Int) {
        AdBlockItemDeleteDialog(index, id, adapter[index].match)
                .show(childFragmentManager, "delete")
    }

    override fun onDelete(index: Int, id: Int) {
        val adBlock = getItem(index, id)
        if (adBlock != null) {
            isModified = true
            adapter.remove(adBlock)
            provider.delete(adBlock.id)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onEdit(index: Int, id: Int) {
        val adBlock = getItem(index, id)
        if (adBlock != null) {
            AdBlockEditDialog(getString(R.string.pref_edit), index, adBlock)
                    .show(childFragmentManager, "edit")
        }
    }

    override fun startMultiSelect(index: Int) {
        actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
        adapter.isMultiSelectMode = true
        adapter.setSelect(index, true)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.ad_block_action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

    override fun onDeleteSelected() {
        val items = adapter.selectedItems
        Collections.sort(items, Collections.reverseOrder())
        for (index in items) {
            isModified = true
            val (id) = adapter.remove(index)
            provider.delete(id)
        }
        actionMode!!.finish()
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                actionMode = mode
                DeleteSelectedDialog().show(childFragmentManager, "delete_selected")
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter.isMultiSelectMode = false
    }

    private fun getItem(index: Int, id: Int): AdBlock? {
        if (index < adapter.itemCount) {
            val adBlock = adapter[index]
            if (adBlock.id == id)
                return adBlock
        }
        return getItemFromId(id)
    }

    private fun getItemFromId(id: Int) = adapter.items.firstOrNull { it.id == id }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ad_block_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity ?: return false
        when (item.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
            R.id.export -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_TITLE, listener!!.getExportFileName(type))
                startActivityForResult(intent, REQUEST_SELECT_EXPORT)
                return true
            }
            R.id.deleteAll -> {
                AdBlockDeleteAllDialog().show(childFragmentManager, "delete_all")
                return true
            }
            R.id.sort_registration -> {
                adapter.items.sortWith(Comparator { m1, m2 -> m1.id.compareTo(m2.id) })
                adapter.notifyDataSetChanged()
                layoutManager.scrollToPosition(0)
                return true
            }
            R.id.sort_registration_reverse -> {
                adapter.items.sortWith(Comparator { m1, m2 -> m2.id.compareTo(m1.id) })
                adapter.notifyDataSetChanged()
                layoutManager.scrollToPosition(0)
                return true
            }
            R.id.sort_name -> {
                adapter.items.sortWith(Comparator { m1, m2 -> m1.match.compareTo(m2.match) })
                adapter.notifyDataSetChanged()
                layoutManager.scrollToPosition(0)
                return true
            }
            R.id.sort_name_reverse -> {
                adapter.items.sortWith(Comparator { m1, m2 -> m2.match.compareTo(m1.match) })
                adapter.notifyDataSetChanged()
                layoutManager.scrollToPosition(0)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDeleteAll() {
        isModified = true
        provider.deleteAll()
        adapter.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as AdBlockFragmentListener
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch(Dispatchers.IO) {
            provider.updateCache()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface AdBlockFragmentListener {
        fun setFragmentTitle(type: Int)

        fun requestImport(uri: Uri)

        fun getExportFileName(type: Int): String
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val REQUEST_SELECT_FILE = 1
        private const val REQUEST_SELECT_EXPORT = 2

        operator fun invoke(type: Int): AdBlockFragment {
            return AdBlockFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, type)
                }
            }
        }
    }
}
