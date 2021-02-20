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

package jp.hazuki.yuzubrowser.legacy.webrtc

import android.webkit.PermissionRequest
import jp.hazuki.yuzubrowser.core.utility.extensions.permissions
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.webrtc.core.PermissionState
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebPermissions
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebRtcRequest

class WebRtcPermission(private val permissionsDao: WebPermissionsDao) {

    private val sitePermissions = hashMapOf<String, WebPermissions>()

    fun requestPermission(permissionRequest: PermissionRequest, webRtcRequest: WebRtcRequest) {
        ui {
            val host = permissionRequest.origin.host
            checkNotNull(host)
            val resources = permissionRequest.resources
            val permissions = permissionRequest.permissions
            val site = sitePermissions[host]
                ?: permissionsDao.get(host)?.also { sitePermissions[host] = it }

            if (site?.match(resources) == true) {
                if (webRtcRequest.requestPermissions(permissions)) {
                    permissionRequest.grant(resources)
                } else {
                    permissionRequest.deny()
                }
            } else {
                if (site == null || site.needRequest(resources)) {
                    webRtcRequest.requestPagePermission(host, resources) { resGranted ->
                        if (resGranted) {
                            ui {
                                if (webRtcRequest.requestPermissions(permissions)) {
                                    val current = if (site != null) {
                                        site.grantAll(resources)
                                        site
                                    } else {
                                        val new = WebPermissions(host, resources)
                                        sitePermissions[host] = new
                                        new
                                    }

                                    permissionsDao.update(current)
                                    permissionRequest.grant(resources)
                                } else {
                                    permissionRequest.deny()
                                }
                            }
                        } else {
                            ui {
                                val current = if (site != null) {
                                    site.denyAll(resources)
                                    site
                                } else {
                                    val new = WebPermissions(host, resources, PermissionState.DENIED)
                                    sitePermissions[host] = new
                                    new
                                }

                                permissionsDao.update(current)
                                permissionRequest.deny()
                            }

                        }
                    }
                } else {
                    val result = site.resources
                    if (result.isEmpty()) {
                        permissionRequest.deny()
                    } else {
                        permissionRequest.grant(result)
                    }
                }
            }
        }
    }

    companion object {
        var instance: WebRtcPermission? = null

        fun getInstance(webPermissionsDao: WebPermissionsDao): WebRtcPermission {
            if (instance == null) {
                instance = WebRtcPermission(webPermissionsDao)
            }
            return instance!!
        }

        fun clearCache() {
            instance?.run { sitePermissions.clear() }
        }
    }
}
