/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.core.lifecycle

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class LiveEvent<T> {

    private val mObserver = mutableSetOf<ObserverWrapper>()

    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }
        val wrapper = ObserverWrapper(observer)
        owner.lifecycle.addObserver(wrapper)
        mObserver += wrapper
    }

    private fun remove(observer: ObserverWrapper) {
        mObserver.remove(observer)
    }

    fun notify(data: T) {
        mObserver.forEach { it.notify(data) }
    }

    private inner class ObserverWrapper(private val observer: Observer<in T>) : LifecycleEventObserver {

        private var isActive = true

        override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                remove(this)
                return
            }

            isActive = isActive(owner)
        }

        fun notify(data: T) {
            if (isActive) observer.onChanged(data)
        }

        fun isActive(owner: LifecycleOwner) =
            owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
}
