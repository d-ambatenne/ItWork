package org.example.project

import android.app.Application

class WeightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // WorkManager initializes automatically via ContentProvider
        // No manual initialization needed
        android.util.Log.d("WeightApp", "Application initialized")
    }
}

