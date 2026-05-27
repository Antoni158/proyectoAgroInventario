package com.example.inventario

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class InventarioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseDatabase.getInstance("https://inventarioagr-default-rtdb.firebaseio.com/")
                .setPersistenceEnabled(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
