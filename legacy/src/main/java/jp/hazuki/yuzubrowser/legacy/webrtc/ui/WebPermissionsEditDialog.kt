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

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.webrtc.core.PermissionState
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebPermissions

class WebPermissionsEditDialog : androidx.fragment.app.DialogFragment() {

    private var listener: OnPermissionEditedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments ?: throw IllegalArgumentException()

        val view = View.inflate(activity, R.layout.dialog_edit_web_permissons, null)
        val camera: Spinner = view.findViewById(R.id.cameraSpinner)
        val mic: Spinner = view.findViewById(R.id.micSpinner)
        val midi: Spinner = view.findViewById(R.id.midiSpinner)
        val mediaId: Spinner = view.findViewById(R.id.mediaIdSpinner)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            midi.visibility = View.GONE
            view.findViewById<View>(R.id.midiTextView).visibility = View.GONE
        }

        val permissions = arguments.getSerializable(ARG_PERMISSION) as WebPermissions
        val host = arguments.getString(ARG_HOST)

        camera.setSelection(permissions.camera.state)
        mic.setSelection(permissions.microphone.state)
        midi.setSelection(permissions.midi.state)
        mediaId.setSelection(permissions.mediaId.state)

        return AlertDialog.Builder(activity)
                .setTitle(host)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    permissions.camera = PermissionState.from(camera.selectedItemPosition)
                    permissions.microphone = PermissionState.from(mic.selectedItemPosition)
                    permissions.midi = PermissionState.from(midi.selectedItemPosition)
                    permissions.mediaId = PermissionState.from(mediaId.selectedItemPosition)

                    listener?.onPermissionEdited(arguments.getInt(ARG_POSITION), host, permissions)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentFragment = parentFragment
        if (parentFragment is OnPermissionEditedListener) {
            listener = parentFragment
            return
        }

        val activity = activity
        if (activity is OnPermissionEditedListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val ARG_POSITION = "pos"
        private const val ARG_HOST = "host"
        private const val ARG_PERMISSION = "permission"

        operator fun invoke(position: Int, host: String, webPermissions: WebPermissions): WebPermissionsEditDialog {
            return WebPermissionsEditDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                    putString(ARG_HOST, host)
                    putSerializable(ARG_PERMISSION, webPermissions)
                }
            }
        }
    }

    interface OnPermissionEditedListener {
        fun onPermissionEdited(position: Int, host: String, permissions: WebPermissions)
    }
}