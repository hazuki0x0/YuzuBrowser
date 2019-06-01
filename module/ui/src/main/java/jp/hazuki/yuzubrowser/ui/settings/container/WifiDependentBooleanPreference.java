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

package jp.hazuki.yuzubrowser.ui.settings.container;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WifiDependentBooleanPreference extends IntContainer {
    public static final int ENABLE = 0;
    public static final int DISABLE = 1;
    public static final int ENABLE_WIFI = 2;

    public WifiDependentBooleanPreference(String name, Integer def_value) {
        super(name, def_value);
    }

    public boolean getBoolean(Context context) {
        switch (get()) {
            case ENABLE:
                return true;
            case DISABLE:
                return false;
            case ENABLE_WIFI: {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    switch (ni.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                        case ConnectivityManager.TYPE_ETHERNET:
                        case ConnectivityManager.TYPE_BLUETOOTH:
                            return true;
                    }
                }
                return false;
            }
            default:
                throw new RuntimeException("Unknown int:" + get());
        }
    }
}
