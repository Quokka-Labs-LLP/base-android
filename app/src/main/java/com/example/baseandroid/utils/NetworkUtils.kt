package com.example.baseandroid.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import com.example.baseandroid.app.MyApp
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {
    /*check is user device connected to internet*/
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = MyApp.getInstance()?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /*check is user device connected to wifi*/
    fun isWifiConnected(): Boolean {
        val connectivityManager = MyApp.getInstance()?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /*check is user device connected to mobile data*/
    fun isMobileDataConnected(): Boolean {
        val connectivityManager = MyApp.getInstance()?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /*check is user device is in Roaming*/
    fun isRoaming(): Boolean {
        val telephonyManager = MyApp.getInstance()?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.isNetworkRoaming
    }

    /*to get the device ip address*/
    fun getIPAddress(): String? {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val addresses = networkInterface.inetAddresses

            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()

                if (!address.isLoopbackAddress && address is InetAddress && address.hostAddress.indexOf(':') < 0) {
                    return address.hostAddress
                }
            }
        }
        return null
    }
}
