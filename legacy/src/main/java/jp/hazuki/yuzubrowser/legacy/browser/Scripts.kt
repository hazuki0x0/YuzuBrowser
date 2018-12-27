/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.browser

object Scripts {
    const val GET_ICON_URL = "(function(){for(var e=document.getElementsByTagName(\"link\"),r=[],l=0;l<e.length;l++){" +
            "var n=e[l].rel;\"apple-touch-icon-precomposed\"!=n&&\"apple-touch-icon\"!=n||r.push(e[l])}" +
            "if(0==r.length)return\"\";for(var t=-1,u=-1,a=null,l=0;l<r.length;l++){var o=r[l].sizes[0];" +
            "if(null==o)a=r[l];else{var h=Number(o[0].match(/\\d+/));h>u&&(t=l,u=h)}}return-1==t?null==a?\"\":a.href:r[t].href}())"
}