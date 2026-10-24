# CodeForge AI — Conversational App & Website Builder

An intelligent, conversational mobile studio for generating, editing, and previewing web applications and code in real time. Powered by Gemini models with high-level architectural thinking, live Google Search grounding, and Firebase cloud persistence.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Summary of Confirmed Choices from Phase 1:**
> - **Primary Output & Live Preview**: Interactive HTML, CSS, and JavaScript real-time web preview rendered inside an optimized, sandboxed Android WebView with instant reload.
> - **Workspace Layout**: Split/adaptive workspace balancing conversational AI guidance, multi-file code editing (`index.html`, `styles.css`, `script.js`, plus Python/Java snippets), and the live interactive preview.
> - **Highlighted Developer Features**: Live console logs with runtime error diagnostics, plus Google Search grounding for up-to-date web libraries, CSS tricks, and APIs.

- **Firebase Backend Provisioning**: Firebase Authentication with Google Sign-In (Credential Manager) and Firestore database will be provisioned via the two-step RPC flow to securely store user projects, code files, and conversation history.
- **Model Specialization Tier**:
  - `gemini-4-argon` with `thinkingLevel = ThinkingLevel.HIGH`: complex architectures, algorithmic logic, and deep refactoring.
  - `gemini-3.8-flash` with Google Search grounding: rapid general generation, modern web component styling, and documentation lookups.
  - `gemini-3.5-flash-lite`: instant code tweaks, syntax fixes, and conversational micro-interactions.

---

## 1. Overview & Core Concept

- **What It Does**: CodeForge AI allows users to build functional web applications and mobile prototypes simply by chatting. You describe your idea (e.g. "Create a retro cyberpunk Pomodoro timer with sound effects and progress ring"), and CodeForge AI generates the complete multi-file project, writes HTML/CSS/JS, streams the progress, and instantly renders the live interactive output in a side-by-side or stacked preview window.
- **Target Audience / Persona**: Developers, creators, students, and prototypers who want to build, test, and iterate on web applications directly from their Android device without needing a local development environment.
- **Key Value**: Eliminates the barrier between ideation and working software on mobile devices through an interactive preview sandbox, conversational iteration, and real-time console feedback.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Authentication & Project Hub**:
   - User signs in seamlessly with Google via Jetpack Credential Manager.
   - The Home Dashboard displays saved projects with live thumbnail badges, last edited timestamps, and starter template shortcuts (e.g., Cyberpunk Snake Game, SaaS Landing Page, Glassmorphic Calculator, Kanban Board).
2. **Conversational Builder Workspace**:
   - The workspace features a 3-mode flexible layout:
     - **Split View**: Synchronized AI chat and live interactive preview.
     - **Code Editor**: Multi-file tab selector (`index.html`, `styles.css`, `script.js`, `main.py`) with syntax highlighting, line numbers, and search.
     - **Full Preview**: Uncluttered full-screen interactive testbed with device viewport toggle (Mobile / Tablet / Desktop scale).
3. **Interactive Preview & Console Diagnostics**:
   - Live web application runs in an isolated WebView sandbox with JavaScript, CSS animations, and DOM APIs enabled.
   - An expandable bottom Console Drawer captures `console.log`, `console.warn`, `console.error`, and uncaught runtime exceptions via a two-way JavaScript bridge.
   - Tapping any error in the console gives an instant "Diagnose & Auto-Fix" button, prompting the AI to patch the code automatically.
4. **AI Customization & Thinking Inspection**:
   - Users can toggle between **High Thinking** (`gemini-4-argon`) for deep problem solving and **Fast Search** (`gemini-3.8-flash`) for docs-backed generation.
   - Thought process inspection reveals architectural reasoning and component breakdown before code generation completes.

### Visual Identity & Theme

- **Aesthetic Direction**: Sleek "Developer Studio" dark mode with cyberpunk violet and electric cyan accents, subtle glassmorphic container cards, and high-contrast monospace code surfaces.
- **Color Palette & Mood**:
  - `Background`: Deep Obsidian `#0D1117` / `#161B22`
  - `Surface / Container`: Elevated Charcoal `#21262D` with subtle `#30363D` borders
  - `Primary Accent`: Electric Cyan `#58A6FF` and Violet Indigo `#8957E5`
  - `Success / Running`: Emerald Neon `#3FB950`
  - `Warning / Console`: Amber Gold `#D29922`
  - `Error / Exception`: Coral Crimson `#F85149`
- **Typography & Hierarchy**:
  - Primary UI: Clean Google Sans / Roboto sans-serif for responsive readability.
  - Code & Console: Clean Monospace font family with consistent line-height and letter-spacing for syntax legibility.
- **Motion & Micro-interactions**:
  - Smooth spring transitions between Chat, Editor, and Preview panes.
  - Animated live-pulse indicator during AI code generation and streaming.
  - Haptic feedback when running code, switching tabs, and copying code snippets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Real-time In-Memory & WebView Sandboxing vs. Remote Server Execution**:
  - *Chosen Approach*: Execute client-side web code (HTML, CSS, modern JS, Canvas, SVG, web APIs) directly inside an Android WebView with a custom asset loader and bidirectional console bridge.
  - *Why*: Instantaneous sub-100ms updates, offline preview capability, zero server latency, and full support for interactive gestures and animations.
  - *Alternatives Considered*: Remote sandboxed containers (e.g. Node runner). Passed over because remote container execution introduces significant network latency, requires external infrastructure, and hurts mobile responsiveness.
- **Decision 2: Multi-File Project Structure**:
  - *Chosen Approach*: Projects maintain discrete `index.html`, `styles.css`, `script.js` files, unified automatically into an optimized bundle on preview compile, while also allowing companion Python or Java code files for multi-language ideation.
  - *Why*: Teaches clean web development standards and allows modular editing rather than dumping all code into an unwieldy single text block.
- **Decision 3: Cloud Firestore for Project Persistence**:
  - *Chosen Approach*: Store user projects, revisions, code files, and chat messages in Cloud Firestore scoped to `request.auth.uid`.
  - *Why*: User projects are high-value creative assets. Cloud storage ensures projects survive app reinstalls and device switches, enabling true multi-device continuity.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│                                                                        │
│   ┌─────────────────────┐  ┌───────────────────┐  ┌────────────────┐   │
│   │   Auth & Projects   │  │ Conversational AI │  │ Multi-File Code│   │
│   │    Hub Screen       │  │   Chat Drawer     │  │   Editor Pane  │   │
│   └──────────┬──────────┘  └─────────┬─────────┘  └───────┬────────┘   │
│              │                       │                    │            │
│              ▼                       ▼                    ▼            │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │          Interactive Live WebView Preview & Console            │   │
│   │  - Android WebView Sandbox        - WebChromeClient Console    │   │
│   │  - Bidirectional JS Bridge        - Viewport Scale Toggle      │   │
│   └──────────────────────────────────┬─────────────────────────────┘   │
└──────────────────────────────────────┼─────────────────────────────────┘
                                       │
                                       ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        ViewModel & Domain Layer                        │
│                                                                        │
│   ┌─────────────────────────┐            ┌─────────────────────────┐   │
│   │     AuthViewModel       │            │    ProjectViewModel     │   │
│   │ (CredentialManager)     │            │ (Project State & Files) │   │
│   └────────────┬────────────┘            └────────────┬────────────┘   │
│                │                                      │                │
│                ▼                                      ▼                │
│   ┌─────────────────────────┐            ┌─────────────────────────┐   │
│   │      AiCodeService      │            │   ProjectRepository     │   │
│   │ - gemini-4-argon        │            │ - Firestore Persistence │   │
│   │ - gemini-3.8-flash      │            │ - Revision Tracking     │   │
│   │ - Search Grounding      │            │                         │   │
│   └─────────────────────────┘            └─────────────────────────┘   │
└──────────────────────────────────────┬─────────────────────────────────┘
                                       │
                                       ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Backend / Cloud Services                        │
│                                                                        │
│   ┌───────────────────────────┐         ┌──────────────────────────┐   │
│   │       Firebase Auth       │         │  Cloud Firestore DB      │   │
│   │   (Google Sign-In Only)   │         │  /users/{uid}/projects   │   │
│   └───────────────────────────┘         └──────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & Firestore Schema

- **Project Document (`/users/{userId}/projects/{projectId}`)**:
  - `id: String` (Document ID)
  - `userId: String` (Owner UID matching `request.auth.uid`)
  - `title: String` (e.g. "Neon Calculator", "Space Dodger Game")
  - `description: String`
  - `htmlContent: String` (HTML5 markup)
  - `cssContent: String` (CSS styling)
  - `jsContent: String` (JavaScript logic)
  - `extraFiles: Map<String, String>` (Optional companion scripts: `main.py`, `App.java`, etc.)
  - `createdAt: Timestamp` (Server Timestamp)
  - `updatedAt: Timestamp` (Server Timestamp)
  - `activeLanguage: String` ("web", "python", "java")

- **ChatMessage Document (`/users/{userId}/projects/{projectId}/messages/{messageId}`)**:
  - `id: String`
  - `sender: String` ("user" | "model")
  - `content: String`
  - `thinkingContent: String?` (Optional thoughts from `gemini-4-argon`)
  - `codeSnippet: String?`
  - `timestamp: Timestamp`

### Key Sequences & Integrations

1. **User Instruction to Live Preview Pipeline**:
   - User types: *"Make the background animated retro stars and add a high score counter that persists in localStorage"*.
   - `AiCodeService` evaluates query complexity:
     - If architectural / complex: invokes `gemini-4-argon` with `thinkingLevel = ThinkingLevel.HIGH`.
     - If docs/API-heavy: invokes `gemini-3.8-flash` with Google Search tool enabled.
   - Streamed response parses updated code files and updates `ProjectViewModel`.
   - The WebView reloads with the newly synthesized bundle; `console.log` messages and errors are piped to the Console state.
2. **One-Tap Console Auto-Fix**:
   - If an error triggers (e.g. `ReferenceError: updateScore is not defined at script.js:42`), a "Fix with AI" chip appears.
   - User taps the chip -> prompt dispatched with stack trace -> AI outputs corrected snippet -> live preview updates without losing app state.
3. **Cloud Sync**:
   - `ProjectRepository` debounces file changes and syncs directly to Cloud Firestore under `/users/{uid}/projects/{projectId}`.
