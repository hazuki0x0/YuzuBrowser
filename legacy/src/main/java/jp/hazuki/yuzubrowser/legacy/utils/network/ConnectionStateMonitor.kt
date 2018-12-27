package jp.hazuki.yuzubrowser.legacy.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class ConnectionStateMonitor(private val listener: OnConnectionChangeListener) : ConnectivityManager.NetworkCallback() {
    private val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

    var isAvailable = false
        private set

    fun enable(context: Context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(networkRequest, this)
    }

    fun disable(context: Context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network?) {
        if (!isAvailable) {
            isAvailable = true
            listener(true)
        }
    }

    override fun onLost(network: Network?) {
        if (isAvailable) {
            isAvailable = false
            listener(false)
        }
    }
}

typealias OnConnectionChangeListener = (isAvailable: Boolean) -> Unit