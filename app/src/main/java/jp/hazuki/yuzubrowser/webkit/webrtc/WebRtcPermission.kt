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

package jp.hazuki.yuzubrowser.webkit.webrtc

import android.webkit.PermissionRequest
import jp.hazuki.yuzubrowser.utils.extensions.permissions
import jp.hazuki.yuzubrowser.utils.ui

class WebRtcPermission {

    private val grantSite = mutableMapOf<String, MutableList<String>>()

    fun requestPermission(permissionRequest: PermissionRequest, webRtcRequest: WebRtcRequest) {
        val host = permissionRequest.origin.host
        val resources = permissionRequest.resources
        val permissions = permissionRequest.permissions

        if (grantSite[host]?.containsAll(resources.toList()) == true) {
            ui {
                if (webRtcRequest.requestPermissions(permissions)) {
                    permissionRequest.grant(resources)
                } else {
                    permissionRequest.deny()
                }
            }
        } else {
            webRtcRequest.requestPagePermission(host, resources) { resGranted ->
                if (resGranted) {
                    ui {
                        if (webRtcRequest.requestPermissions(permissions)) {
                            val site = grantSite[host]
                            if (site != null) {
                                site.addAll(resources)
                            } else {
                                grantSite[host] = resources.toMutableList()
                            }
                            permissionRequest.grant(resources)
                        } else {
                            permissionRequest.deny()
                        }
                    }
                } else {
                    permissionRequest.deny()
                }
            }
        }
    }

    companion object {
        val instance by lazy { WebRtcPermission() }
    }
}