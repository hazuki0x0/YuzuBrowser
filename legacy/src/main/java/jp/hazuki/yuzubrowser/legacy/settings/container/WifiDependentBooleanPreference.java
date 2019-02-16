package jp.hazuki.yuzubrowser.legacy.settings.container;

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
