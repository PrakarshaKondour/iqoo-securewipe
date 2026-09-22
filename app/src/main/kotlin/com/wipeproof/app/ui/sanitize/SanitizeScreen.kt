package com.wipeproof.app.ui.sanitize

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.core.model.SanitizationMethod
import com.wipeproof.app.ui.components.DemoBanner
import com.wipeproof.app.ui.components.ProgressTimeline
import com.wipeproof.app.ui.components.TimelineItem
import com.wipeproof.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanitizeScreen(
    caseId: String,
    onSanitizationDone: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SanitizeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(caseId) {
        viewModel.loadCase(caseId)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Step 3 of 6: SANITIZE", style = MaterialTheme.typography.titleMedium, color = PrimaryTeal)
                        Text("Guided Sanitization Execution", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is SanitizeUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryTeal)
                        }
                    }
                }
                is SanitizeUiState.Ready -> {
                    if (state.case_.isDemoMode) {
                        item { DemoBanner() }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Ready to Sanitize",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Method: ${state.method.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryTeal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.method.detailedDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (state.method == SanitizationMethod.FACTORY_RESET) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, PrimaryTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Android OS Factory Reset Protocol",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "1. Tap 'Open Android Reset Settings' below.\n2. Confirm 'Erase all data (factory reset)' in System Settings.\n3. The device TEE will discard hardware keys.\n4. Return here after reset or proceed with state audit.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = {
                                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, tint = PrimaryTeal)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open Android System Settings", color = PrimaryTeal)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { viewModel.startSanitization() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "START SANITIZATION",
                                fontWeight = FontWeight.Bold,
                                color = DarkBg,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is SanitizeUiState.Running -> {
                    if (state.case_.isDemoMode) {
                        item { DemoBanner() }
                    }

                    item {
                        ProgressTimeline(
                            percentage = state.progress.percentage,
                            currentStep = state.progress.step
                        )
                    }

                    item {
                        Text(
                            text = "LIVE AUDIT EVENT CHAIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(state.recentEvents) { event ->
                        TimelineItem(event = event)
                    }
                }
                is SanitizeUiState.Completed -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, VerifiedGreen, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = VerifiedGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Sanitization Execution Complete",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All media passes concluded. The operation has entered terminal state.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onSanitizationDone(state.case_.caseId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text(
                                text = "PROCEED TO TECHNICAL VERIFICATION",
                                fontWeight = FontWeight.Bold,
                                color = DarkBg,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is SanitizeUiState.Error -> {
                    item {
                        Text(
                            text = "Sanitization Failed: ${state.message}",
                            color = TamperedRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
