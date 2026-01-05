package com.thomas.cargotracker

import android.app.Application
import com.thomas.cargotracker.di.AppContainer

class CargoTrackerApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
