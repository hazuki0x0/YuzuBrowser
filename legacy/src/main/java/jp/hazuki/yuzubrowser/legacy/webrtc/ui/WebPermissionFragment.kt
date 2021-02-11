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

package jp.hazuki.yuzubrowser.legacy.webrtc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.webrtc.WebPermissionsDatabase
import jp.hazuki.yuzubrowser.legacy.webrtc.WebRtcPermission
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebPermissions
import jp.hazuki.yuzubrowser.ui.widget.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class WebPermissionFragment : androidx.fragment.app.Fragment(), OnRecyclerListener, WebPermissionsEditDialog.OnPermissionEditedListener {

    private lateinit var adapter: WebPermissionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return

        adapter = WebPermissionAdapter(activity,
            WebPermissionsDatabase.getInstance(activity.applicationContext).getList().toMutableList(),
            this)

        view.findViewById<RecyclerView>(R.id.recyclerView).run {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity))
            adapter = this@WebPermissionFragment.adapter
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val (host, permissions) = adapter[position]
        WebPermissionsEditDialog(position, host, permissions)
                .show(childFragmentManager, "edit")
    }

    override fun onPermissionEdited(position: Int, host: String, permissions: WebPermissions) {
        val activity = activity ?: return

        adapter[position] = Pair(host, permissions)
        adapter.notifyItemChanged(position)

        WebPermissionsDatabase.getInstance(activity.applicationContext).update(host, permissions)
        WebRtcPermission.clearCache()
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int) = false
}
