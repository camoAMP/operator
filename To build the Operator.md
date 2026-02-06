To build the **Operator** app—a local Android assistant that functions as a "smart macro runner with memory"—you need to implement a modular architecture that combines accessibility automation, a JSON-driven workflow engine, and a secure artifact storage system.

The following sections provide the complete working skeleton and runnable Kotlin code snippets based on the sources.

### 1. Project Layout and Data Models
The project is organized into five functional areas: **accessibility** (automation), **workflow** (execution), **scheduler** (timing), **storage** (memory), and **ui** (control).

**WorkflowStep Data Model:**
This model defines individual actions within a workflow, including support for file metadata.
```kotlin
data class WorkflowStep(
    val action: String, // e.g., "CLICK", "SET_TEXT", "SAVE_FILE"
    val resourceId: String? = null,
    val value: String? = null,
    val artifactType: String? = null, // e.g., "output", "image"
    val extension: String? = null // e.g., "txt", "png"
)
```

### 2. The "Hands": Accessibility Automation
The **`OperatorService`** interacts with whitelisted apps by finding UI nodes and performing physical actions.

```kotlin
class OperatorService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    /** Find a node by resource-id and perform a click */
    fun clickByResourceId(resourceId: String) {
        val rootNode = rootInActiveWindow ?: return
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
        if (nodes.isNotEmpty()) {
            nodes.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    /** Find a node by resource-id and set text via Bundle */
    fun setTextByResourceId(resourceId: String, text: String) {
        val rootNode = rootInActiveWindow ?: return
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
        if (nodes.isNotEmpty()) {
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            nodes.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }
    }

    /** Scroll a node forward by resource-id */
    fun scrollByResourceId(resourceId: String) {
        val rootNode = rootInActiveWindow ?: return
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
        if (nodes.isNotEmpty()) {
            nodes.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
    }
}
```

### 3. The "Brain": Workflow Engine and Runner
The **`WorkflowRunner`** iterates through the JSON-defined steps and enforces the **Artifact Naming Policy** for any saved files.

```kotlin
class WorkflowRunner(private val service: OperatorService) {
    fun run(steps: List<WorkflowStep>, projectId: String, workflowId: String) {
        for (step in steps) {
            when (step.action) {
                "CLICK" -> step.resourceId?.let { service.clickByResourceId(it) }
                "SET_TEXT" -> step.resourceId?.let { service.setTextByResourceId(it, step.value ?: "") }
                "SCROLL" -> step.resourceId?.let { service.scrollByResourceId(it) }
                "SAVE_FILE" -> {
                    val fileName = ArtifactNamer.generateFileName(
                        projectId = projectId,
                        workflowId = workflowId,
                        artifactType = step.artifactType ?: "output",
                        extension = step.extension ?: "txt"
                    )
                    saveToFile(fileName, step.value ?: "")
                }
            }
        }
    }

    private fun saveToFile(fileName: String, content: String) {
        val file = File(fileName)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}
```

### 4. The "Memory": Artifact Naming Policy
This component ensures all generated outputs (text, images, CAD files) are saved with consistent, timestamped names.

```kotlin
object ArtifactNamer {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    fun generateFileName(projectId: String, workflowId: String, artifactType: String, extension: String): String {
        val timestamp = dateFormat.format(Date())
        return "Operator/Projects/$projectId/$workflowId/${artifactType}_$timestamp.$extension"
    }
}
```

### 5. Local Quantized Model (User-Provided)
If you use a local quantized AI model, the model weights are not bundled in this repo.
Each user must download and install their own model files for their chosen local LLM runtime.
Keep model paths in local configuration and do not commit model files to version control.

### 6. The Scheduler: WorkManager Integration
Workflows can be queued to run at specific times or when **environmental conditions** (like Wi-Fi or charging) are met.

```kotlin
class OperatorScheduler(private val context: Context) {
    fun scheduleWorkflow(workflowId: String, delayMillis: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi requirement
            .setRequiresCharging(true)
            .build()

        val request = OneTimeWorkRequestBuilder<WorkflowWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
```

### 7. Workflow Schema (JSON Example)
This JSON defines a sequence to interact with ChatGPT and save the result.
```json
{
  "workflowId": "chatgpt_haiku",
  "name": "ChatGPT Haiku Workflow",
  "steps": [
    { "action": "OPEN_APP", "package": "com.openai.chatgpt" },
    { "action": "SET_TEXT", "resourceId": "com.openai.chatgpt:id/message_input", "value": "Write a haiku about spring" },
    { "action": "CLICK", "resourceId": "com.openai.chatgpt:id/send_button" },
    { "action": "SAVE_FILE", "artifactType": "output", "extension": "txt" }
  ]
}
```

### 8. Safe Auto-Post Policy
To ensure security, your implementation must adhere to these fail-safe rules:
*   **Whitelisting:** Only interact with pre-approved packages.
*   **Consent Checkpoints:** Stop automation before tapping "Post" or "Share" unless the specific action is whitelisted.
*   **Operational Halt:** If the `OperatorService` cannot find the nodes defined in the JSON, or if it encounters a password field, it must **automatically abort** the execution to prevent errors.
