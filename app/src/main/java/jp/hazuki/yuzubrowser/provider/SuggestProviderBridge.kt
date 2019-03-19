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

package jp.hazuki.yuzubrowser.provider

import android.net.Uri
import jp.hazuki.yuzubrowser.ui.provider.ISuggestProvider

class SuggestProviderBridge : ISuggestProvider {
    override val uriNet: Uri
        get() = SuggestProvider.URI_NET
    override val uriLocal: Uri
        get() = SuggestProvider.URI_LOCAL
    override val uriNormal: Uri
        get() = SuggestProvider.URI_NORMAL
    override val uriNone: Uri
        get() = SuggestProvider.URI_NONE
    override val suggestHistory: String
        get() = SuggestProvider.SUGGEST_HISTORY
}