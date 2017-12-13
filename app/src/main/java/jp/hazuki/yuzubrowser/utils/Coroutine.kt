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

package jp.hazuki.yuzubrowser.utils

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlin.coroutines.experimental.CoroutineContext
import kotlinx.coroutines.experimental.async as ktAsync

typealias Deferred<T> = kotlinx.coroutines.experimental.Deferred<T>

fun <T> async(context: CoroutineContext = CommonPool, start: CoroutineStart = CoroutineStart.DEFAULT, parent: Job? = null, block: suspend CoroutineScope.() -> T)
        = ktAsync(context, start, parent, block)

fun ui(start: CoroutineStart = CoroutineStart.DEFAULT, parent: Job? = null, block: suspend CoroutineScope.() -> Unit)
        = launch(UI, start, parent, block)