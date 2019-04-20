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

package jp.hazuki.yuzubrowser.adblock.ui.abp

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.filter.abp.getAbpBlackListFile
import jp.hazuki.yuzubrowser.adblock.filter.abp.getAbpWhiteListFile
import jp.hazuki.yuzubrowser.adblock.filter.abp.getAbpWhitePageListFile
import jp.hazuki.yuzubrowser.adblock.filter.abp.isNeedUpdate
import jp.hazuki.yuzubrowser.adblock.filter.unified.getFilterDir
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpDatabase
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity
import jp.hazuki.yuzubrowser.adblock.service.AbpUpdateService
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.fragment_abp_list.*
import javax.inject.Inject

class AbpFragment : DaggerFragment(), OnRecyclerListener, AddAbpDialog.OnAddItemListener, AbpMenuDialog.OnAbpMenuListener, AbpItemDeleteDialog.OnAbpItemDeleteListener {

    @Inject
    internal lateinit var abpDatabase: AbpDatabase

    private lateinit var adapter: AbpEntityAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_abp_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: throw IllegalStateException()
        adapter = AbpEntityAdapter(activity, mutableListOf(), this@AbpFragment)
        ui {
            adapter.items.addAll(abpDatabase.abpDao().getAll())
            adapter.notifyDataSetChanged()
        }
        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@AbpFragment.adapter
        }
        fab.setOnClickListener {
            AddAbpDialog.create(null)
                    .show(childFragmentManager, "edit")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.abp_fragment, menu)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val entity = adapter.items[position]
        entity.enabled = !entity.enabled
        ui { abpDatabase.abpDao().update(entity) }
        adapter.notifyItemChanged(position)
        if (entity.enabled && entity.isNeedUpdate()) {
            AbpUpdateService.update(activity!!, entity, result)
        }
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        AbpMenuDialog(position, adapter.items[position])
                .show(childFragmentManager, "menu")
        return true
    }

    override fun onAddEntity(entity: AbpEntity) {
        ui {
            if (entity.entityId > 0) {
                abpDatabase.abpDao().update(entity)
            } else {
                entity.entityId = abpDatabase.abpDao().inset(entity).toInt()
            }
            AbpUpdateService.update(activity!!, entity, result)
        }
    }

    override fun onAskDelete(index: Int, entity: AbpEntity) {
        AbpItemDeleteDialog(index, entity).show(childFragmentManager, "delete")
    }

    override fun onEdit(index: Int, entity: AbpEntity) {
        AddAbpDialog.create(entity).show(childFragmentManager, "edit")
    }

    override fun onRefresh(index: Int, entity: AbpEntity) {
        AbpUpdateService.update(activity!!, entity, result)
    }

    override fun onDelete(index: Int, entity: AbpEntity) {
        ui {
            abpDatabase.abpDao().delete(entity)
            val dir = activity!!.getFilterDir()
            dir.getAbpBlackListFile(entity).delete()
            dir.getAbpWhiteListFile(entity).delete()
            dir.getAbpWhitePageListFile(entity).delete()
            val i = adapter.items.indexOf(entity)
            adapter.items.removeAt(i)
            adapter.notifyItemRemoved(i)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.update -> {
                AbpUpdateService.updateAll(activity!!, true, result)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val result = object : AbpUpdateService.UpdateResult(Handler()) {
        override fun onUpdated(entity: AbpEntity) {
            updateInternal(entity)
        }

        override fun onFailedUpdate(entity: AbpEntity) {
            updateInternal(entity)
        }

        private fun updateInternal(entity: AbpEntity) {
            val index = adapter.items.indexOf(entity)
            if (index < 0) {
                adapter.items.add(entity)
                adapter.notifyItemChanged(adapter.itemCount - 1)
            } else {
                adapter.items[index] = entity
                adapter.notifyItemChanged(index)
            }
        }

        override fun onUpdateAll() {
            ui {
                adapter.items.clear()
                adapter.items.addAll(abpDatabase.abpDao().getAll())
                adapter.notifyDataSetChanged()
            }
        }
    }
}
