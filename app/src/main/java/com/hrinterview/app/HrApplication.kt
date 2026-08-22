package com.hrinterview.app

import android.app.Application
import com.hrinterview.app.data.local.db.AppDatabase
import com.hrinterview.app.data.local.datastore.SettingsDataStore
import com.hrinterview.app.data.repository.HrRepository
import com.hrinterview.app.session.InterviewSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppContainer(app: Application) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database = AppDatabase.create(app)
    val settings = SettingsDataStore(app)
    val repository = HrRepository(database)
    private val _session = MutableStateFlow(InterviewSession())
    val session: StateFlow<InterviewSession> = _session

    init {
        scope.launch { repository.ensureSeeded() }
    }

    fun updateSession(transform: (InterviewSession) -> InterviewSession) {
        _session.value = transform(_session.value)
    }

    fun setSession(value: InterviewSession) {
        _session.value = value
    }
}

class HrApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Application.container: AppContainer
    get() = (this as HrApplication).container
