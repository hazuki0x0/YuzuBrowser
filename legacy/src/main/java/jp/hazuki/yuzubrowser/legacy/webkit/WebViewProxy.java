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

package jp.hazuki.yuzubrowser.legacy.webkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.util.ArrayMap;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.core.utility.log.Logger;
import jp.hazuki.yuzubrowser.legacy.R;

public class WebViewProxy {
    private static final Pattern pattern = Pattern.compile("\\s*(.+?):(\\d+)\\s*");

    private static boolean setProxy = false;

    public static void resetProxy(Context context) {
        if (setProxy) setProxy(context, "", 0, "", 0);
    }

    private static boolean resetProxyInternal(Context context) {
        return setProxy(context, "", 0, "", 0);
    }

    @SuppressLint("PrivateApi")
    private static boolean setProxy(Context context, String host, int port, String httpsHost, int httpsPort) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", Integer.toString(port));
        System.setProperty("https.proxyHost", httpsHost);
        System.setProperty("https.proxyPort", Integer.toString(httpsPort));
        try {
            Class applicationClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applicationClass.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object loadedApk = mLoadedApkField.get(context);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            mReceiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) mReceiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    Class<?> clazz = receiver.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        onReceiveMethod.invoke(receiver, context, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | NullPointerException e) {
            Logger.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: ", e.toString());
            setProxy = false;
        }
        return false;
    }

    public static boolean setProxy(Context context, boolean enable, String proxy_address, boolean httpsEnable, String httpsProxyAdress) {
        if (setProxy && !WebViewProxy.resetProxyInternal(context.getApplicationContext()))
            return false;

        setProxy = false;

        if (enable) {
            Matcher matcher = pattern.matcher(proxy_address);
            if (matcher.find()) {
                String httpHost = matcher.group(1);
                int httpPort = Integer.parseInt(matcher.group(2), 10);
                String httpsHost = httpHost;
                int httpsPort = httpPort;
                if (httpsEnable) {
                    Matcher httpsMatcher = pattern.matcher(httpsProxyAdress);
                    if (httpsMatcher.find()) {
                        httpsHost = httpsMatcher.group(1);
                        httpsPort = Integer.parseInt(httpsMatcher.group(2), 10);
                    } else {
                        httpsHost = "";
                        httpsPort = 0;
                    }
                }
                setProxy = true;
                return WebViewProxy.setProxy(context.getApplicationContext(), httpHost, httpPort, httpsHost, httpsPort);
            } else {
                Toast.makeText(context, R.string.proxy_address_error, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }
}
