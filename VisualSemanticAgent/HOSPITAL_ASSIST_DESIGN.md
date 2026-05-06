# Hospital Assist Design

## Positioning

`VisualSemanticAgent` is being narrowed from a generic multimodal demo into a
hospital outpatient assistive agent for blind and low-vision users.

The target is not "take a photo and read a screen". The target is:

1. continuous perception instead of manual snapshot capture
2. non-visual interaction instead of visual UI dependency
3. executable intent instead of plain recognition output
4. confirmation-based action instead of direct auto execution

## Core Scenario

Primary scenario: outpatient visit assistance in hospitals.

Typical user goal:

1. The user says: "Help me read this notice."
2. The system opens continuous camera sensing.
3. The system gives audio guidance until text is stable.
4. The system extracts structured medical visit information.
5. The system asks for confirmation before creating reminders or navigation.

## Engineering Architecture

### 1. Perception Layer

- continuous video stream
- automatic frame quality scoring
- target existence detection
- frame stability observation

Current code anchors:

- `camera/CameraManager.kt`
- `decision/ContinuousVisionCoordinator.kt`

### 2. Semantic Parsing Layer

- OCR / multimodal semantic extraction
- structured slots: time, location, event title, answer
- action normalization into executable schema

Current code anchors:

- `network/VLMNetworkClient.kt`
- `utils/ResponseInterpreter.kt`
- `decision/ExecutableIntent.kt`

### 3. Decision Layer

- temporal voting over multiple frames
- confidence thresholding
- confirmation requirement based on risk level
- fallback and retry prompts

Current code anchors:

- `decision/TemporalIntentStabilizer.kt`
- `decision/ContinuousVisionCoordinator.kt`

### 4. Action Layer

- create reminder/calendar event
- open navigation
- prepare SMS
- TTS feedback

Current code anchors:

- `intent/IntentDispatcher.kt`
- `tts/TextToSpeechManager.kt`

## Why It Is Non-Visual

The final interaction assumption is:

- the user cannot rely on screen layout
- the user may not be able to frame the object accurately
- the user should not need to press a shutter button

Therefore the system interaction must be:

- voice first
- continuous sensing
- auto-capture after stability passes
- spoken feedback for every state transition

The on-screen Compose UI remains useful for development, debugging, and judges'
visual inspection, but it is not the primary user interaction contract.

## Executable Intent Schema

The system converts model output into an `ExecutableIntent`.

Schema fields:

- `scene`
- `action`
- `title`
- `time`
- `location`
- `answer`
- `description`
- `phoneNumber`
- `confidence`
- `requiresConfirmation`
- `riskLevel`

Scene constant:

- `hospital_outpatient_assist`

Risk policy:

- `create_event`, `navigate`, `send_sms` -> high risk, confirmation required
- `tts_feedback` -> low risk, can be spoken directly

## Temporal Stability Mechanism

This is the main engineering credibility point.

The system does not trust a single frame.

Instead, it uses temporal voting:

1. each frame produces a candidate executable intent
2. only frames above confidence threshold `tau` are considered valid
3. only when the latest result matches the same stability key for `N` consecutive frames
4. the system transitions to `READY_FOR_CONFIRMATION`

Current default parameters in the code skeleton:

- `N = 3`
- `tau = 0.78`
- history window size `= 5`

This directly addresses:

- accidental blur
- transient OCR noise
- single-frame hallucination

## Continuous Capture Mechanism

The system should be described as "always looking" rather than "take one photo".

The coordinator checks:

- readable text exists
- exposure is acceptable
- sharpness is acceptable
- target position is centered enough
- target area is large enough
- candidate intent is stable enough

Only then:

- a stable frame is considered auto-captured
- parsing result is spoken
- a confirmation question is asked

## Audio Guidance Loop

The guidance loop is the core usability mechanism.

Examples:

- "No readable text detected yet. Slowly pan the phone to search for the notice."
- "The target is on the right. Move the phone slightly right."
- "The image is blurred. Please hold the phone steady for a moment."
- "Text detected. Keep holding still while I verify the result."
- "Stable result detected. Capturing and parsing now."

This transforms visual framing into an audio feedback loop.

## Failure Handling

The system must never fail silently.

Fallback prompts:

- no target found
- blur too high
- exposure too low
- target too small
- unstable semantic result
- timeout and retry

Suggested user-facing prompts:

- "No valid notice detected. Please adjust the angle."
- "The environment is too dark. Please move to a brighter place."
- "I am not confident about the result yet. Would you like me to keep trying?"

## Confirmation First

High-risk actions must follow:

1. explain what was understood
2. ask for confirmation
3. execute only after confirmation

Example:

- "I detected a medical examination notice for tomorrow at 9 AM on the third floor imaging department. Would you like me to create a reminder?"

This is the safety guarantee of the system.

## Baseline Comparison

Recommended comparison targets in presentation:

- Microsoft Seeing AI
- Google Lookout

Suggested contrast:

| Dimension | Existing tools | This project |
| --- | --- | --- |
| Recognition mode | Single-shot / passive reading | Continuous perception |
| Interaction | Describe only | Confirmable dialogue |
| Actionability | Mostly informational | Executable actions |
| Safety policy | Limited action confirmation | Explicit confirm-before-execute |

## Current Prototype Status

Already available:

- Android app shell
- camera preview and capture path
- voice input and TTS output
- mock model response pipeline
- system action dispatch
- executable intent schema skeleton
- temporal stabilizer skeleton
- continuous sensing coordinator skeleton

Not yet fully implemented:

- real frame quality scoring from live camera frames
- actual OCR region tracking for direction estimation
- wake phrase / hardware shortcut entry
- full non-visual production interaction loop

## Recommended Demo Flow

1. User says: "Help me read this notice."
2. System starts continuous sensing.
3. System says: "Text detected. Keep holding still while I verify the result."
4. System says: "Stable result detected. Capturing and parsing now."
5. System says: "I detected a medical check notice. Tomorrow at 9 AM. Third floor imaging department. Would you like me to create a reminder?"
6. User says: "Yes."
7. System creates the reminder and speaks the completion result.
