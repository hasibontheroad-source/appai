package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Project
import com.example.data.model.ProjectTemplates
import com.example.ui.auth.AuthViewModel
import com.example.ui.project.ProjectViewModel
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PurpleBrand
import com.example.ui.theme.PurpleBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProjectsListScreen(
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    onProjectSelected: (Project) -> Unit
) {
    val projects by projectViewModel.projectsList.collectAsState()
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = CyanBrand,
                contentColor = Color(0xFF0D1117),
                modifier = Modifier.testTag("new_project_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Project",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardDark,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.codeforge_logo_1792878652238),
                            contentDescription = "CodeForge AI Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                text = "CodeForge AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "AI App & Web Studio",
                                fontSize = 11.sp,
                                color = CyanBrand
                            )
                        }
                    }

                    // User Info & Sign Out
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Developer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        IconButton(
                            onClick = { authViewModel.signOut(context) },
                            modifier = Modifier.size(32.dp).testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Projects List or Empty State with Templates
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: My Projects
                item {
                    Text(
                        text = "MY PROJECTS (${projects.size})",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                if (projects.isEmpty()) {
                    item {
                        EmptyProjectsBanner(
                            onCreateNew = { showNewProjectDialog = true }
                        )
                    }
                } else {
                    items(projects, key = { it.id }) { proj ->
                        ProjectItemCard(
                            project = proj,
                            onClick = { onProjectSelected(proj) },
                            onDelete = { projectToDelete = proj }
                        )
                    }
                }

                // Section: Starter Templates
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "STARTER TEMPLATES",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PurpleBright,
                        letterSpacing = 1.sp
                    )
                }

                items(ProjectTemplates.ALL_TEMPLATES) { template ->
                    TemplateItemCard(
                        template = template,
                        onLaunch = {
                            projectViewModel.createNewProject(template.title, template)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = {
                Text(
                    text = "Create New Project",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter a name for your application or website:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = newProjectTitle,
                        onValueChange = { newProjectTitle = it },
                        placeholder = { Text("e.g. Neon Weather Dashboard") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_project_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBrand,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = newProjectTitle.trim()
                        projectViewModel.createNewProject(title)
                        newProjectTitle = ""
                        showNewProjectDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_project_button")
                ) {
                    Text("Create", color = CyanBrand, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark
        )
    }

    // Delete Confirmation Dialog
    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete '${projectToDelete?.title}'? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        projectToDelete?.id?.let { projectViewModel.deleteProject(it) }
                        projectToDelete = null
                    }
                ) {
                    Text("Delete", color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark
        )
    }
}

@Composable
private fun ProjectItemCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("project_card_${project.id}"),
        color = CardDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyanBrand.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = null,
                        tint = CyanBrand,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = project.title.ifBlank { "Untitled Project" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = project.description.ifBlank { "HTML5, CSS, and JavaScript Web App" },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateItemCard(
    template: Project,
    onLaunch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PurpleBrand.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onLaunch),
        color = Color(0xFF161B22)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleBrand.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = PurpleBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = template.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = template.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PurpleBrand.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Launch",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleBright
                )
            }
        }
    }
}

@Composable
private fun EmptyProjectsBanner(onCreateNew: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = CyanBrand,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "No saved projects yet",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Tap a starter template below or create a blank project to start coding with AI!",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
