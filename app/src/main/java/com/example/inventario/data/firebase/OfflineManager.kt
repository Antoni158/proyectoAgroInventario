package com.example.inventario.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object OfflineManager {

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun enqueueSync(context: Context, op: SyncOperation) {
        SyncQueueManager(context).enqueue(op)
        if (isOnline(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                SyncQueueManager(context).processPending()
            }
        }
    }

    suspend fun syncIfOnline(context: Context): Int {
        if (!isOnline(context)) return 0
        return SyncQueueManager(context).processPending()
    }
}
