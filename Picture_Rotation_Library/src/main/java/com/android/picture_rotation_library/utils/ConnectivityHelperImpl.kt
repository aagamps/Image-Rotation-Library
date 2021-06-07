package com.kredily.web.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Helper class to get status of mobile device connectivity
 * This class implements the contract specified by [com.kredily.web.utils.ConnectivityHelper]
 *
 * @author Ketan Patel
 */
class ConnectivityHelperImpl(private val mContext: Context) : ConnectivityHelper {

    /**
     * Get the ConnectivityManager
     *
     * @return [ConnectivityManager]
     */
    private val connectivityManager: ConnectivityManager?
        get() {
            return mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }


    /**
     * Check if there is any internet connectivity
     *
     * @return true when the device is connected to any wifi/data network false otherwise
     */
    override fun isConnected(): Boolean {
        var isConnected = false
        val manager = connectivityManager
        if(null != manager) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.let {
                    it.getNetworkCapabilities(manager.activeNetwork)?.apply {
                        isConnected = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            else -> false
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = manager.activeNetworkInfo
                @Suppress("DEPRECATION")
                isConnected = networkInfo != null && networkInfo.isConnected
            }
        }
        return isConnected
    }
}