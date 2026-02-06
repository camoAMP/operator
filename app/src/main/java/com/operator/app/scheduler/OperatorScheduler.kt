package com.operator.app.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class OperatorScheduler(private val context: Context) {
    fun scheduleWorkflow(workflowId: String, projectId: String, delayMillis: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val request = OneTimeWorkRequestBuilder<WorkflowWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    WorkflowWorker.KEY_WORKFLOW_ID to workflowId,
                    WorkflowWorker.KEY_PROJECT_ID to projectId
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
