package com.operator.app.scheduler

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.operator.app.accessibility.OperatorService
import com.operator.app.storage.WorkflowQueue
import com.operator.app.storage.WorkflowRequest

class WorkflowWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result {
        val workflowId = inputData.getString(KEY_WORKFLOW_ID) ?: return Result.failure()
        val projectId = inputData.getString(KEY_PROJECT_ID) ?: return Result.failure()

        WorkflowQueue(applicationContext).enqueue(
            WorkflowRequest(projectId = projectId, workflowId = workflowId)
        )

        val intent = Intent(OperatorService.ACTION_WORKFLOW_ENQUEUED)
            .setPackage(applicationContext.packageName)
        applicationContext.sendBroadcast(intent)

        return Result.success()
    }

    companion object {
        const val KEY_WORKFLOW_ID = "workflowId"
        const val KEY_PROJECT_ID = "projectId"
    }
}
