package com.operator.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import com.operator.app.OperatorPolicy
import com.operator.app.storage.ArtifactStore
import com.operator.app.storage.WorkflowQueue
import com.operator.app.storage.WorkflowRequest
import com.operator.app.workflow.WorkflowRepository
import com.operator.app.workflow.WorkflowRunner
import java.util.Locale
import java.util.concurrent.Executors

class OperatorService : AccessibilityService() {
    private val executor = Executors.newSingleThreadExecutor()
    private val queue by lazy { WorkflowQueue(this) }
    private lateinit var policy: OperatorPolicy

    @Volatile
    private var aborted = false

    private val workflowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_WORKFLOW_ENQUEUED) {
                drainQueue()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        policy = OperatorPolicy.default(packageName)

        val info = serviceInfo
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        serviceInfo = info

        ContextCompat.registerReceiver(
            this,
            workflowReceiver,
            IntentFilter(ACTION_WORKFLOW_ENQUEUED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        drainQueue()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: actions are initiated by workflows.
    }

    override fun onInterrupt() {
        abort("Service interrupted")
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(workflowReceiver) }
        executor.shutdown()
        instance = null
        super.onDestroy()
    }

    fun isAborted(): Boolean = aborted

    fun resetAbort() {
        aborted = false
    }

    fun abort(reason: String) {
        aborted = true
        Log.w(TAG, reason)
    }

    fun openApp(targetPackage: String): Boolean {
        if (!policy.isPackageAllowed(targetPackage)) {
            abort("Package not allowed: $targetPackage")
            return false
        }
        val intent = packageManager.getLaunchIntentForPackage(targetPackage)
        if (intent == null) {
            abort("No launch intent for $targetPackage")
            return false
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        return true
    }

    fun clickByResourceId(resourceId: String): Boolean {
        val node = findNode(resourceId) ?: return false
        if (isConsentSensitive(node) && !policy.isAutoPostAllowed(resourceId)) {
            abort("Consent checkpoint hit for $resourceId")
            return false
        }
        val ok = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (!ok) abort("Click failed: $resourceId")
        return ok
    }

    fun setTextByResourceId(resourceId: String, text: String): Boolean {
        val node = findNode(resourceId) ?: return false
        val args = Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )
        val ok = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        if (!ok) abort("Set text failed: $resourceId")
        return ok
    }

    fun scrollByResourceId(resourceId: String): Boolean {
        val node = findNode(resourceId) ?: return false
        val ok = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        if (!ok) abort("Scroll failed: $resourceId")
        return ok
    }

    fun runWorkflow(request: WorkflowRequest) {
        executor.execute {
            resetAbort()
            try {
                val repo = WorkflowRepository(this)
                val workflow = repo.loadFromAssets(request.workflowId)
                val runner = WorkflowRunner(this, ArtifactStore(this))
                runner.run(workflow.steps, request.projectId, workflow.workflowId)
            } catch (e: Exception) {
                abort("Workflow load/run failed: ${e.message}")
            }
        }
    }

    private fun drainQueue() {
        val pending = queue.drain()
        for (request in pending) {
            runWorkflow(request)
        }
    }

    private fun ensureAllowedPackage(): Boolean {
        val root = rootInActiveWindow ?: run {
            abort("No active window")
            return false
        }
        val pkg = root.packageName?.toString()
        if (!policy.isPackageAllowed(pkg)) {
            abort("Package not allowed: $pkg")
            return false
        }
        return true
    }

    private fun findNode(resourceId: String): AccessibilityNodeInfo? {
        if (!ensureAllowedPackage()) return null
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByViewId(resourceId)
        if (nodes.isNullOrEmpty()) {
            abort("Node not found: $resourceId")
            return null
        }
        val node = nodes.first()
        if (node.isPassword) {
            abort("Password field encountered: $resourceId")
            return null
        }
        return node
    }

    private fun isConsentSensitive(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.trim()?.lowercase(Locale.US)
        val desc = node.contentDescription?.toString()?.trim()?.lowercase(Locale.US)
        return text == "post" || text == "share" || desc == "post" || desc == "share"
    }

    companion object {
        private const val TAG = "OperatorService"
        const val ACTION_WORKFLOW_ENQUEUED = "com.operator.app.WORKFLOW_ENQUEUED"

        @Volatile
        var instance: OperatorService? = null
            private set
    }
}
