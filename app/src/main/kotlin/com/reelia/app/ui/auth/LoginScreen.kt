package com.reelia.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelia.app.R
import com.reelia.app.ui.common.components.PasswordField
import com.reelia.app.ui.guide.GuideScreen
import com.reelia.app.ui.theme.AppBackground
import com.reelia.app.ui.theme.StatusFavorite
import com.reelia.app.ui.theme.StatusWantToWatch
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showGuide by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.onScreenEntered() }

    if (showGuide) {
        GuideScreen(onBack = { showGuide = false })
        return
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            StatusWantToWatch.copy(alpha = 0.18f),
                            StatusFavorite.copy(alpha = 0.10f),
                            AppBackground,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp)),
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.login_title),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(
                        if (uiState.isSignUpMode) R.string.login_subtitle_sign_up else R.string.login_subtitle_sign_in,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text(stringResource(R.string.login_email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                PasswordField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = stringResource(R.string.login_password_label),
                    imeAction = ImeAction.Done,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                if (uiState.isSignUpMode && uiState.password.isNotEmpty()) {
                    PasswordStrengthMeter(uiState.password)
                }

                val errorText = uiState.errorMessageRes?.let { stringResource(it) } ?: uiState.errorMessage
                errorText?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Button(
                    onClick = viewModel::onSubmit,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            stringResource(
                                if (uiState.isSignUpMode) R.string.login_sign_up_button else R.string.login_sign_in_button,
                            ),
                        )
                    }
                }

                TextButton(
                    onClick = viewModel::onToggleMode,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(
                        stringResource(
                            if (uiState.isSignUpMode) R.string.login_toggle_to_sign_in else R.string.login_toggle_to_sign_up,
                        ),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.login_or_divider),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                viewModel.onGoogleIdTokenReceived(fetchGoogleIdToken(context))
                            } catch (e: Exception) {
                                viewModel.onGoogleSignInFailed(e.message)
                            }
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.login_google_button))
                }

                TextButton(
                    onClick = { showGuide = true },
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.login_learn_more_button))
                }
            }
        }
    }
}
