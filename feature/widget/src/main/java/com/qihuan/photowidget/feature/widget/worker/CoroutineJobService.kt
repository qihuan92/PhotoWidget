package com.qihuan.photowidget.feature.widget.worker

import android.app.job.JobParameters
import android.app.job.JobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
abstract class CoroutineJobService : JobService(), CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main.immediate

    abstract suspend fun startJob(params: JobParameters?)

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            startJob(params)
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        job.cancel()
        return false
    }
}