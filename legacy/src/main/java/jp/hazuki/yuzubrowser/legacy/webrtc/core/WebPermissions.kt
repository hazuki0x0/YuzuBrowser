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

package jp.hazuki.yuzubrowser.legacy.webrtc.core

import android.os.Build
import android.webkit.PermissionRequest
import java.io.Serializable

data class WebPermissions(
        var id: Long = -1,
        var camera: PermissionState = PermissionState.UNCONFIGURED,
        var microphone: PermissionState = PermissionState.UNCONFIGURED,
        var midi: PermissionState = PermissionState.UNCONFIGURED,
        var mediaId: PermissionState = PermissionState.UNCONFIGURED
) : Serializable {

    val resources: Array<String>
        get() {
            val list = mutableListOf<String>()
            if (camera === PermissionState.GRANTED) {
                list.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            }
            if (microphone === PermissionState.GRANTED) {
                list.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && midi === PermissionState.GRANTED) {
                list.add(PermissionRequest.RESOURCE_MIDI_SYSEX)
            }
            if (mediaId === PermissionState.GRANTED) {
                list.add(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)
            }
            return list.toTypedArray()
        }

    fun grantAll(resources: Array<String>) {
        setPermissions(this, resources, PermissionState.GRANTED)
    }

    fun denyAll(resources: Array<String>) {
        setPermissions(this, resources, PermissionState.DENIED)
    }

    fun match(resources: Array<String>): Boolean {
        for (item in resources) {
            when (item) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> if (camera !== PermissionState.GRANTED) return false
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> if (microphone !== PermissionState.GRANTED) return false
                PermissionRequest.RESOURCE_MIDI_SYSEX -> if (midi !== PermissionState.GRANTED) return false
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> if (mediaId !== PermissionState.GRANTED) return false
            }
        }
        return true
    }

    fun needRequest(resources: Array<String>): Boolean {
        for (item in resources) {
            when (item) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> if (camera === PermissionState.UNCONFIGURED) return true
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> if (microphone === PermissionState.UNCONFIGURED) return true
                PermissionRequest.RESOURCE_MIDI_SYSEX -> if (midi === PermissionState.UNCONFIGURED) return true
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> if (mediaId === PermissionState.UNCONFIGURED) return true
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) return true

        if (other is WebPermissions) {
            return camera === other.camera && microphone === other.microphone && midi === other.midi && mediaId === other.mediaId
        }
        return false
    }

    override fun hashCode(): Int {
        return (camera.state) +
                (microphone.state shl 2) +
                (midi.state shl 4) +
                (mediaId.state shl 6)
    }

    companion object {
        operator fun invoke(resources: Array<String>, state: PermissionState = PermissionState.GRANTED): WebPermissions {
            return WebPermissions().also { setPermissions(it, resources, state) }
        }

        private fun setPermissions(permissions: WebPermissions, resources: Array<String>, state: PermissionState) {
            for (item in resources) {
                when (item) {
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE -> permissions.camera = state
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE -> permissions.microphone = state
                    PermissionRequest.RESOURCE_MIDI_SYSEX -> permissions.midi = state
                    PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> permissions.mediaId = state
                }
            }
        }
    }
}