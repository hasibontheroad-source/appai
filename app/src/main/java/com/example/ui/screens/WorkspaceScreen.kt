package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiGenerationState
import com.example.ai.ModelTier
import com.example.data.model.ChatMessage
import com.example.data.model.LogLevel
import com.example.data.model.Project
import com.example.ui.components.CodeEditorView
import com.example.ui.components.ConsoleDrawer
import com.example.ui.components.WebViewPreview
import com.example.ui.project.FileTab
import com.example.ui.project.ProjectViewModel
import com.example.ui.project.WorkspaceView
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PurpleBrand
import com.example.ui.theme.PurpleBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WorkspaceScreen(
    viewModel: ProjectViewModel,
    project: Project,
    onBack: () -> Unit
) {
    val activeView by viewModel.activeWorkspaceView.collectAsState()
    val activeTab by viewModel.activeFileTab.collectAsState()
    val modelTier by viewModel.modelTier.collectAsState()
    val isSearchGrounding by viewModel.isSearchGrounding.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    val consoleLogs by viewModel.consoleLogs.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isConsoleDrawerOpen by viewModel.isConsoleDrawerOpen.collectAsState()
    val reloadTrigger by viewModel.reloadTrigger.collectAsState()

    val errorCount = remember(consoleLogs) { consoleLogs.count { it.level == LogLevel.ERROR } }
    var promptInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            WorkspaceTopBar(
                title = project.title,
                activeView = activeView,
                modelTier = modelTier,
                isSearchGrounding = isSearchGrounding,
                onBack = onBack,
                onViewSelected = { viewModel.setWorkspaceView(it) },
                onToggleTier = {
                    val next = if (modelTier == ModelTier.HIGH_THINKING) ModelTier.GENERAL_SEARCH else ModelTier.HIGH_THINKING
                    viewModel.setModelTier(next)
                },
                onToggleSearch = { viewModel.toggleSearchGrounding() },
                onSave = { viewModel.saveCurrentProject() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main View Area: Responsive Split, Editor, or Preview
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth >= 700.dp

                when (activeView) {
                    WorkspaceView.PREVIEW -> {
                        WebViewPreview(
                            project = project,
                            reloadTrigger = reloadTrigger,
                            errorCount = errorCount,
                            onLogReceived = { level, msg -> viewModel.addConsoleLog(level, msg) },
                            onToggleConsole = { viewModel.toggleConsoleDrawer() },
                            onForceReload = { viewModel.forceReloadPreview() }
                        )
                    }
                    WorkspaceView.EDITOR -> {
                        CodeEditorView(
                            project = project,
                            activeTab = activeTab,
                            onTabSelected = { viewModel.setFileTab(it) },
                            onCodeChanged = { viewModel.updateActiveCode(it) },
                            onRunCode = {
                                viewModel.saveCurrentProject()
                                viewModel.setWorkspaceView(WorkspaceView.PREVIEW)
                            }
                        )
                    }
                    WorkspaceView.SPLIT -> {
                        if (isWide) {
                            // Side-by-side on wide screens / tablets
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1.1f)) {
                                    WebViewPreview(
                                        project = project,
                                        reloadTrigger = reloadTrigger,
                                        errorCount = errorCount,
                                        onLogReceived = { level, msg -> viewModel.addConsoleLog(level, msg) },
                                        onToggleConsole = { viewModel.toggleConsoleDrawer() },
                                        onForceReload = { viewModel.forceReloadPreview() }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .background(BorderDark)
                                )
                                Box(modifier = Modifier.weight(0.9f)) {
                                    ChatConversationalPanel(
                                        messages = chatMessages,
                                        aiState = aiState,
                                        modelTier = modelTier,
                                        promptInput = promptInput,
                                        onPromptChanged = { promptInput = it },
                                        onSend = {
                                            viewModel.sendPrompt(promptInput)
                                            promptInput = ""
                                        }
                                    )
                                }
                            }
                        } else {
                            // Stacked on phones: Preview top, Chat bottom
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1.1f)) {
                                    WebViewPreview(
                                        project = project,
                                        reloadTrigger = reloadTrigger,
                                        errorCount = errorCount,
                                        onLogReceived = { level, msg -> viewModel.addConsoleLog(level, msg) },
                                        onToggleConsole = { viewModel.toggleConsoleDrawer() },
                                        onForceReload = { viewModel.forceReloadPreview() }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(BorderDark)
                                )
                                Box(modifier = Modifier.weight(0.9f)) {
                                    ChatConversationalPanel(
                                        messages = chatMessages,
                                        aiState = aiState,
                                        modelTier = modelTier,
                                        promptInput = promptInput,
                                        onPromptChanged = { promptInput = it },
                                        onSend = {
                                            viewModel.sendPrompt(promptInput)
                                            promptInput = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Console Drawer
            AnimatedVisibility(
                visible = isConsoleDrawerOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ConsoleDrawer(
                    logs = consoleLogs,
                    onClearLogs = { viewModel.clearConsoleLogs() },
                    onClose = { viewModel.toggleConsoleDrawer() },
                    onFixWithAi = { errorMsg -> viewModel.diagnoseAndFixError(errorMsg) }
                )
            }
        }
    }
}

@Composable
private fun WorkspaceTopBar(
    title: String,
    activeView: WorkspaceView,
    modelTier: ModelTier,
    isSearchGrounding: Boolean,
    onBack: () -> Unit,
    onViewSelected: (WorkspaceView) -> Unit,
    onToggleTier: () -> Unit,
    onToggleSearch: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp).testTag("workspace_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyanBrand,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title.ifBlank { "CodeForge Project" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                // Action Controls: Save & Model indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Thinking mode toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (modelTier == ModelTier.HIGH_THINKING) PurpleBrand.copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (modelTier == ModelTier.HIGH_THINKING) PurpleBrand else BorderDark,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(onClick = onToggleTier)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (modelTier == ModelTier.HIGH_THINKING) PurpleBright else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (modelTier == ModelTier.HIGH_THINKING) "Thinking HIGH" else "Fast Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (modelTier == ModelTier.HIGH_THINKING) PurpleBright else TextSecondary
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = onSave,
                        modifier = Modifier.size(34.dp).testTag("save_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Project",
                            tint = CyanBrand,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Mode Selector Bar (Split, Editor, Preview)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeTab("Split View", Icons.Default.ViewAgenda, activeView == WorkspaceView.SPLIT) {
                    onViewSelected(WorkspaceView.SPLIT)
                }
                ModeTab("Code Editor", Icons.Default.Code, activeView == WorkspaceView.EDITOR) {
                    onViewSelected(WorkspaceView.EDITOR)
                }
                ModeTab("Live Sandbox", Icons.Default.Visibility, activeView == WorkspaceView.PREVIEW) {
                    onViewSelected(WorkspaceView.PREVIEW)
                }
            }
        }
    }
}

@Composable
private fun ModeTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CyanBrand.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) CyanBrand.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) CyanBrand else TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) CyanBrand else TextSecondary
        )
    }
}

@Composable
fun ChatConversationalPanel(
    messages: List<ChatMessage>,
    aiState: AiGenerationState,
    modelTier: ModelTier,
    promptInput: String,
    onPromptChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, aiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Chat messages stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty() && aiState is AiGenerationState.Idle) {
                item {
                    EmptyChatGreeting()
                }
            }

            items(messages, key = { it.id.ifBlank { it.hashCode().toString() } }) { msg ->
                ChatBubble(msg)
            }

            // Real-time generation streaming indicator
            if (aiState is AiGenerationState.Generating) {
                item {
                    StreamingProgressBubble(aiState.partialText)
                }
            } else if (aiState is AiGenerationState.Error) {
                item {
                    ErrorBubble(aiState.message)
                }
            }
        }

        // Quick action prompt suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickPromptChip("✨ Add Neon Glow") { onPromptChanged("Make the elements have glowing neon borders and animated gradients.") }
            QuickPromptChip("🎮 Add Sound Effects") { onPromptChanged("Add Web Audio API synthesizers so button clicks and actions produce retro synth beeps.") }
            QuickPromptChip("📱 Responsive Mobile") { onPromptChanged("Optimize CSS layout for mobile touchscreens with responsive padding.") }
            QuickPromptChip("💾 Save to LocalStorage") { onPromptChanged("Save all state into browser localStorage so progress survives reloads.") }
        }

        // Prompt Input Field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = onPromptChanged,
                    placeholder = {
                        Text(
                            text = "Ask AI to code, style, or fix...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanBrand,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                val isGenerating = aiState is AiGenerationState.Generating

                IconButton(
                    onClick = onSend,
                    enabled = promptInput.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (promptInput.isNotBlank() && !isGenerating) CyanBrand else Color(0xFF21262D))
                        .testTag("send_prompt_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyanBrand,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (promptInput.isNotBlank()) Color(0xFF0D1117) else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatGreeting() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = PurpleBright,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CodeForge Conversational Engine",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Tell the AI what you want to build or modify. Watch code write and render in real-time!",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) CyanBrand else Color(0xFF21262D))
                .border(
                    1.dp,
                    if (isUser) CyanBrand else BorderDark,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.content,
                fontSize = 12.sp,
                color = if (isUser) Color(0xFF0D1117) else TextPrimary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun StreamingProgressBubble(partial: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF21262D))
            .border(1.dp, PurpleBrand.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = PurpleBright,
                    strokeWidth = 1.5.dp
                )
                Text(
                    text = "AI Coding in Progress...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleBright
                )
            }
            Text(
                text = partial.takeLast(250),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun ErrorBubble(errorText: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NeonRed.copy(alpha = 0.15f))
            .border(1.dp, NeonRed, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "Error: $errorText",
            fontSize = 12.sp,
            color = NeonRed
        )
    }
}

@Composable
private fun QuickPromptChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = CyanBrand,
            fontWeight = FontWeight.Medium
        )
    }
}
