package jp.hazuki.yuzubrowser.webkit;

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

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.Logger;

public class WebViewProxy {
    private static final Pattern pattern = Pattern.compile("\\s*(.+?):(\\d+)\\s*");

    public static boolean resetProxy(Context context) {
        return setProxy(context, "", 0);
    }

    public static boolean setProxy(Context context, String host, int port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", Integer.toString(port));
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", Integer.toString(port));
        try {
            Class applictionClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applictionClass.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object mloadedApk = mLoadedApkField.get(context);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            mReceiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) mReceiversField.get(mloadedApk);
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
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        }
        return false;
    }

    public static boolean setProxy(Context context, boolean enable, String proxy_address) {
        if (!WebViewProxy.resetProxy(context))
            return false;

        if (enable) {
            Matcher matcher = pattern.matcher(proxy_address);
            if (matcher.find()) {
                return WebViewProxy.setProxy(context, matcher.group(1), Integer.parseInt(matcher.group(2), 10));
            } else {
                Toast.makeText(context, R.string.proxy_address_error, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }
}
