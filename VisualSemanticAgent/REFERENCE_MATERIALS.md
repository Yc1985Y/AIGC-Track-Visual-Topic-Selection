# Reference Materials

## Local Mirrors

The following reference repositories have been pulled into a separate local
folder for ongoing product and engineering comparison:

- `E:\AIGC\reference-materials\Ruoyi-Android-App`
- `E:\AIGC\reference-materials\RuoYi-App-vue3`
- `E:\AIGC\reference-materials\RuoYi-App-V3-TS`

They are intentionally kept outside the competition project so the main repo is
not polluted by third-party source trees.

## Why These Three

### 1. `Ruoyi-Android-App`

Most useful as the closest native Android shell reference.

Best parts to borrow during later development:

- activity / fragment level app shell organization
- settings, mine, and workbench page decomposition
- adapter / widget packaging style
- repository + service + request / entity layering
- general mobile app navigation and account area composition

Paths worth revisiting:

- `E:\AIGC\reference-materials\Ruoyi-Android-App\app\src\main\java\com\ruoyi\app\activity`
- `E:\AIGC\reference-materials\Ruoyi-Android-App\app\src\main\java\com\ruoyi\app\fragment`
- `E:\AIGC\reference-materials\Ruoyi-Android-App\app\src\main\java\com\ruoyi\app\api`

### 2. `RuoYi-App-vue3`

Most useful as a lightweight mobile information architecture reference.

Best parts to borrow conceptually:

- work / mine / common page grouping
- common mobile dashboard structure
- settings and personal-center information density
- page routing decomposition for app-like flows

Paths worth revisiting:

- `E:\AIGC\reference-materials\RuoYi-App-vue3\pages\work`
- `E:\AIGC\reference-materials\RuoYi-App-vue3\pages\mine`
- `E:\AIGC\reference-materials\RuoYi-App-vue3\store`

### 3. `RuoYi-App-V3-TS`

Most useful as a more modern engineering organization reference.

Best parts to borrow conceptually:

- `services` and `stores` separation
- `components` + `composables` style decomposition
- page-level module split for medium-size mobile products
- typed project organization ideas

Paths worth revisiting:

- `E:\AIGC\reference-materials\RuoYi-App-V3-TS\src\pages`
- `E:\AIGC\reference-materials\RuoYi-App-V3-TS\src\stores`
- `E:\AIGC\reference-materials\RuoYi-App-V3-TS\src\services`

## What We Can Borrow

These repos are valuable for:

- app shell structure
- workbench / mine / settings page patterns
- list-card layout rhythm
- bottom navigation information architecture
- account and preference page organization
- network layer and repository layering ideas
- reusable mobile UI module decomposition

## What We Should Not Copy As Core Logic

The competition value of `VisualSemanticAgent` does not come from generic CRUD
mobile scaffolding. The following capabilities must remain self-built and
scenario-specific:

- continuous camera perception
- temporal voting and stability judgement
- voice-first non-visual interaction loop
- executable intent schema
- confirmation-before-execution decision flow
- visual-to-tool action routing logic
- multimodal understanding to Android action dispatch

## Practical Use In This Project

Planned borrowing direction:

1. use `Ruoyi-Android-App` as the main reference for native Android page shell,
   settings area, and modular package layout
2. use `RuoYi-App-vue3` as the reference for workbench grouping and mobile
   information density
3. use `RuoYi-App-V3-TS` as the reference for cleaner module boundaries and
   reusable service / state organization

## Rule For Future Changes

If a future UI or app-shell change is inspired by these materials, prefer:

- adapting structure, hierarchy, and component ideas
- redesigning visuals to match the Visual-to-Tool OS product direction
- keeping the non-visual interaction model as the first-class product contract

The app should feel like an execution-aware agent, not a generic admin client.
