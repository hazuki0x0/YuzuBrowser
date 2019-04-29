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

package jp.hazuki.asyncpermissions

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class PermissionResult {

    val permission: String
        get() = permissions[0]

    abstract val permissions: List<String>

    data class Granted(override val permissions: List<String>) : PermissionResult()

    data class ShouldShowRationale(
            override val permissions: List<String>,
            private val fragment: AsyncPermissionsFragment
    ) : PermissionResult() {

        suspend fun proceed(): PermissionResult = suspendCancellableCoroutine { cont ->
            fragment.requestFromRationale(*permissions.toTypedArray(), cont = cont)
        }

        suspend fun cancel(): PermissionResult = suspendCancellableCoroutine { cont ->
            Denied(permissions).let { cont.resume(it) }
        }
    }

    data class Denied(override val permissions: List<String>) : PermissionResult()

    data class NeverAskAgain(override val permissions: List<String>) : PermissionResult()
}
