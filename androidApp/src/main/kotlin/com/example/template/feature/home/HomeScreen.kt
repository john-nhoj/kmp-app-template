package com.example.template.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.template.R
import com.example.template.feature.home.presentation.HomeUiState
import com.example.template.feature.home.presentation.HomeViewModel
import com.example.template.ui.theme.TemplateTheme
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: wires the shared [HomeViewModel] to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(modifier: Modifier = Modifier, viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState, onRefresh = viewModel::onRefresh, modifier = modifier)
}

/** Stateless and previewable: renders [uiState] and reports user intents through callbacks. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(uiState: HomeUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.isError -> HomeError(onRetry = onRefresh)
                else -> HomeGreeting(platformName = uiState.platformName)
            }
        }
    }
}

@Composable
private fun HomeGreeting(platformName: String?, modifier: Modifier = Modifier) {
    val greeting = platformName
        ?.let { stringResource(R.string.home_greeting, it) }
        ?: stringResource(R.string.home_greeting_generic)

    Text(
        text = greeting,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun HomeError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenContentPreview() {
    TemplateTheme {
        HomeScreen(uiState = HomeUiState(isLoading = false, platformName = "Android 16"), onRefresh = {})
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenErrorPreview() {
    TemplateTheme {
        HomeScreen(uiState = HomeUiState(isLoading = false, isError = true), onRefresh = {})
    }
}
