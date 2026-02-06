package com.operator.app.workflow

import com.operator.app.accessibility.OperatorService
import com.operator.app.storage.ArtifactNamer
import com.operator.app.storage.ArtifactStore
import java.util.Locale

class WorkflowRunner(
    private val service: OperatorService,
    private val artifactStore: ArtifactStore
) {
    fun run(steps: List<WorkflowStep>, projectId: String, workflowId: String): Boolean {
        service.resetAbort()
        for (step in steps) {
            if (service.isAborted()) return false
            val action = step.action.uppercase(Locale.US)
            val ok = when (action) {
                "OPEN_APP" -> {
                    val pkg = step.packageName ?: return abort("Missing package for OPEN_APP")
                    service.openApp(pkg)
                }
                "CLICK" -> {
                    val id = step.resourceId ?: return abort("Missing resourceId for CLICK")
                    service.clickByResourceId(id)
                }
                "SET_TEXT" -> {
                    val id = step.resourceId ?: return abort("Missing resourceId for SET_TEXT")
                    service.setTextByResourceId(id, step.value ?: "")
                }
                "SCROLL" -> {
                    val id = step.resourceId ?: return abort("Missing resourceId for SCROLL")
                    service.scrollByResourceId(id)
                }
                "SAVE_FILE" -> {
                    val fileName = ArtifactNamer.generateRelativePath(
                        projectId = projectId,
                        workflowId = workflowId,
                        artifactType = step.artifactType ?: "output",
                        extension = step.extension ?: "txt"
                    )
                    artifactStore.writeText(fileName, step.value ?: "")
                    true
                }
                else -> abort("Unknown action: ${step.action}")
            }
            if (!ok) return false
        }
        return true
    }

    private fun abort(reason: String): Boolean {
        service.abort(reason)
        return false
    }
}
