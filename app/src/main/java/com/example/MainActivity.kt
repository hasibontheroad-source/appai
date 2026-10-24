package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.project.ProjectViewModel
import com.example.ui.screens.ProjectsListScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.CodeForgeTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            CodeForgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        projectViewModel = projectViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    projectViewModel: ProjectViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val activeProject by projectViewModel.activeProject.collectAsState()

    if (authState !is AuthUiState.Authenticated) {
        // Auth gate: render login screen with Google Sign-In button
        SignInScreen(authViewModel = authViewModel)
    } else {
        // Authenticated user session
        if (activeProject != null) {
            // Intercept system back button to exit workspace to project list
            BackHandler {
                projectViewModel.saveCurrentProject()
                projectViewModel.clearActiveProject()
            }

            WorkspaceScreen(
                viewModel = projectViewModel,
                project = activeProject!!,
                onBack = {
                    projectViewModel.saveCurrentProject()
                    projectViewModel.clearActiveProject()
                }
            )
        } else {
            ProjectsListScreen(
                projectViewModel = projectViewModel,
                authViewModel = authViewModel,
                onProjectSelected = { proj ->
                    projectViewModel.selectProject(proj)
                }
            )
        }
    }
}
