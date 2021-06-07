package com.kredily.web.utils

/**
 * Contract to be implemented by the ConnectivityManager
 *
 * @author Ketan Patel
 */
interface ConnectivityHelper {

    /**
     * Check if there is any connectivity
     *
     * @return true when the device is connected to any data network false otherwise
     */
    fun isConnected(): Boolean

}