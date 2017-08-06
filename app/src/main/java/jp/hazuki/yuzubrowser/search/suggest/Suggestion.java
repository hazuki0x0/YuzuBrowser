/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.search.suggest;

public class Suggestion {
    public Suggestion(String word) {
        this.word = word;
        this.history = false;
    }

    public Suggestion(String word, boolean history) {
        this.word = word;
        this.history = history;
    }

    public final String word;
    public final boolean history;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Suggestion) {
            if (word != null) {
                return word.equals(((Suggestion) obj).word);
            } else {
                return ((Suggestion) obj).word == null;
            }
        }
        return false;
    }
}
