# Visual-to-Tool OS Design

## Positioning

`VisualSemanticAgent` is framed as a **mobile Visual-to-Tool Agent Middleware**.

- the camera captures fragmented real-world information
- the model converts visual content into structured intent
- the client applies risk control and validation
- the operating system tools are launched only after the result is judged executable

The project is not a simple OCR app or a free-form multimodal chatbot.
It is an execution-aware middleware prototype for mobile AI interaction.

## Product Definition

The goal is to compress a traditional cross-app workflow:

1. see and understand a poster / notice / sign
2. memorize the key details
3. open another app
4. manually re-enter the information

into a shorter loop:

1. capture or point at the content
2. extract structured intent
3. confirm if needed
4. launch the target system capability

## Architecture

### 1. Perception Layer

Input sources:

- live camera preview frames
- single capture images
- ASR text from voice input
- manual text input

Responsibilities:

- collect multimodal input
- support future OCR block localization
- support future blur / exposure / layout pre-check
- provide the upstream signal for temporal stability judgment

Current code anchors:

- `camera/CameraManager.kt`
- `decision/ContinuousVisionCoordinator.kt`

### 2. LLM Routing Layer

The cloud model works as a semantic router instead of a free-form assistant.

Responsibilities:

- extract structured entities
- map content into action categories
- produce confidence
- generate clarification prompts when uncertain

Current code anchors:

- `network/VLMNetworkClient.kt`
- `utils/ResponseInterpreter.kt`
- `decision/ExecutableIntent.kt`

### 3. Execution and Interaction Layer

The client does not execute from raw model output directly.

It adds:

- field validation
- fused confidence scoring
- risk policy
- confirmation card interaction
- Android intent dispatch

Current code anchors:

- `decision/ActionValidator.kt`
- `decision/RiskPolicyEngine.kt`
- `intent/IntentDispatcher.kt`
- `MainActivity.kt`

## Structured Intent Contract

The model is constrained to return strict JSON:

```json
{
  "action": "create_event | navigate | send_sms | tts_feedback | clarification | unknown",
  "confidence": 0.85,
  "payload": {
    "title": "提取或生成的标题",
    "time": "严格 ISO 8601 时间",
    "location": "具体地点或 POI",
    "phone_number": "纯数字字符串",
    "description": "详细上下文",
    "answer": "适合播报的摘要"
  },
  "fallback_query": "低置信度时向用户追问的话术",
  "target_found": true
}
```

This schema is the backbone of the whole execution chain.

## Extract-Suggest-Confirm-Execute

### Extract

The app sends image or text input and receives structured JSON.

### Suggest

The client checks:

- required fields
- fused confidence
- risk thresholds
- whether to speak, confirm, or clarify

### Confirm

Medium-risk and high-risk actions must go through a confirmation card.

### Execute

Only validated actions are mapped into Android system tools:

- calendar insert
- map navigation
- SMS draft
- native TTS

## Risk Policy

The current policy is action-based.

- `send_sms`: high risk, never auto-send
- `create_event`: medium risk, confirmation required
- `navigate`: medium risk, confirmation required
- `tts_feedback`: low risk, direct TTS allowed

Invalid payloads or low-confidence results are rerouted to `clarification`.

## Fused Confidence

The system does not trust model confidence alone.

It combines:

- model confidence
- payload completeness
- visual quality confidence
- temporal stability confidence

This is why the project should be described as an execution-aware middleware.

## Temporal Stability

Single-frame outputs are fragile in real scenes.

The project already contains a stabilizer for continuous perception:

- multi-frame observation
- stability key matching
- threshold filtering
- ready-for-confirmation transition

Current code anchors:

- `decision/TemporalIntentStabilizer.kt`
- `decision/ContinuousVisionCoordinator.kt`

## Typical Scenarios

### 1. Poster or Notice -> `create_event`

Examples:

- lecture poster
- exam notice
- seminar announcement

Goal:

- extract title, time, location
- create a calendar entry after confirmation

### 2. Real-world Address or Sign -> `navigate`

Examples:

- building sign
- classroom notice
- meeting venue

Goal:

- resolve a destination
- launch map intent after confirmation

### 3. Phone Number or Contact Label -> `send_sms`

Examples:

- device maintenance plate
- service hotline
- temporary coordination notice

Goal:

- extract phone number and message draft
- open SMS draft only

### 4. Dense Text or Complex Layout -> `tts_feedback`

Examples:

- menu board
- study material
- long notice

Goal:

- compress long-form text
- speak only the useful summary
