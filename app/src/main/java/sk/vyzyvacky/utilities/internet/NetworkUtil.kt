package sk.vyzyvacky.utilities.internet

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


enum class ConnectionType {
    WIFI, MOBILE, NOT_CONNECTED
}

class NetworkUtil {
    companion object {
        fun getConnectivityStatus(context: Context): ConnectionType {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            if (null != capabilities) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return ConnectionType.WIFI
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return ConnectionType.MOBILE
            }
            return ConnectionType.NOT_CONNECTED
        }
    }
}