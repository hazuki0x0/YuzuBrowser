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

package jp.hazuki.yuzubrowser.legacy.utils.handler

import android.os.Handler
import android.os.Message
import java.util.*

abstract class PauseHandler : Handler() {

    /**
     * Message Queue Buffer
     */
    private val messageQueueBuffer = Vector<Message>()

    /**
     * Flag indicating the pause state
     */
    private var paused: Boolean = false

    /**
     * Resume the handler
     */
    fun resume() {
        paused = false

        while (messageQueueBuffer.size > 0) {
            val msg = messageQueueBuffer.elementAt(0)
            messageQueueBuffer.removeElementAt(0)
            sendMessage(msg)
        }
    }

    /**
     * Pause the handler
     */
    fun pause() {
        paused = true
    }

    /**
     * Notification that the message is about to be stored as the activity is
     * paused. If not handled the message will be saved and replayed when the
     * activity resumes.
     *
     * @param message the message which optional can be handled
     * @return true if the message is to be stored
     */
    protected abstract fun storeMessage(message: Message): Boolean

    /**
     * Notification message to be processed. This will either be directly from
     * handleMessage or played back from a saved message when the activity was
     * paused.
     *
     * @param message the message to be handled
     */
    protected abstract fun processMessage(message: Message)

    /**
     * {@inheritDoc}
     */
    override fun handleMessage(msg: Message) {
        if (paused) {
            if (storeMessage(msg)) {
                val msgCopy = Message()
                msgCopy.copyFrom(msg)
                messageQueueBuffer.add(msgCopy)
            }
        } else {
            processMessage(msg)
        }
    }


}