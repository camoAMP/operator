# Operator

Local Android assistant that runs JSON-defined workflows through accessibility actions and stores outputs as artifacts.

## What We Did
- Implemented a workflow engine (`WorkflowRunner`) with JSON parsing (`WorkflowJsonParser`).
- Added artifact storage with deterministic naming (`ArtifactStore`, `ArtifactNamer`).
- Added workflow queue storage using `SharedPreferences` (`WorkflowQueue`).
- Added accessibility automation (`OperatorService`) and a sample workflow (`chatgpt_haiku.json`).
- Added unit tests for JSON parsing edge cases in `app/src/test/java/com/operator/app/workflow/WorkflowJsonParserTest.kt`.
- Added an in-app note and build doc note about local quantized model files being user-provided.

## Local Quantized Model Note
Local quantized model weights are **not** bundled in this repo. Each user must download and install their own model files for their chosen local LLM runtime. Keep model paths in local configuration and do not commit model files.

## Memory Storage
- Artifacts: stored in app-internal storage under `context.filesDir`, using paths like `Operator/Projects/<project>/<workflow>/...`.
- Workflow queue: stored in `SharedPreferences` under `operator_workflow_queue`.

## Build
```bash
./gradlew assembleDebug
```

## Tests
```bash
./gradlew testDebugUnitTest
```

## Project Layout
- `app/src/main/java/com/operator/app/accessibility/`: accessibility automation
- `app/src/main/java/com/operator/app/workflow/`: workflow models + runner + JSON parser
- `app/src/main/java/com/operator/app/storage/`: artifact storage + queue
- `app/src/main/assets/workflows/`: sample workflows

