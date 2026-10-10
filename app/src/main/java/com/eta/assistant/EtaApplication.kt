package com.eta.assistant

import android.app.Application
import com.eta.assistant.data.local.EtaDatabase
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.EtaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EtaApplication : Application() {
    lateinit var database: EtaDatabase
        private set

    lateinit var repository: EtaRepository
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = EtaDatabase.getDatabase(this)
        repository = EtaRepository(database)
        preferencesRepository = UserPreferencesRepository(this)
        com.eta.assistant.core.util.NotificationHelper.createNotificationChannel(this)

        // Prepopulate demo data matching the UI mockup if first run
        CoroutineScope(Dispatchers.IO).launch {
            repository.seedDemoDataIfEmpty()
        }
    }
}

