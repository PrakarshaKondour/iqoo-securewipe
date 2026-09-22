package com.wipeproof.app.ui.handover

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.domain.usecase.HandoverCheck
import com.wipeproof.app.ui.components.DemoBanner
import com.wipeproof.app.ui.components.TimelineItem
import com.wipeproof.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoverScreen(
    caseId: String,
    onBackHome: () -> Unit,
    viewModel: HandoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(caseId) {
        viewModel.loadPassport(caseId)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Step 6 of 6: HANDOVER", style = MaterialTheme.typography.titleMedium, color = PrimaryTeal)
                        Text("Sanitization Passport & Chain of Custody", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HandoverUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
            is HandoverUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TamperedRed)
                }
            }
            is HandoverUiState.Display -> {
                val passport = state.passport
                val isReady = passport.readiness.isReady

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (passport.case_.isDemoMode) {
                        item { DemoBanner() }
                    }

                    item {
                        // Handover Readiness Banner
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, if (isReady) VerifiedGreen else TamperedRed, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isReady) VerifiedGreenBg.copy(alpha = 0.4f) else TamperedRedBg.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isReady) VerifiedGreen else TamperedRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isReady) "SAFE TO HAND OVER ✓" else "NOT READY FOR HANDOVER ✕",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isReady) VerifiedGreen else TamperedRed
                                    )
                                    Text(
                                        text = if (isReady) "All technical criteria and chain-of-custody proofs verified." else "Unresolved risks or missing verification checks detected.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "HANDOVER READINESS CHECKLIST",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(passport.readiness.checks) { check ->
                        HandoverCheckItem(check = check)
                    }

                    item {
                        Text(
                            text = "SANITIZATION PASSPORT (CHAIN OF CUSTODY)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(passport.events) { event ->
                        TimelineItem(
                            event = event,
                            isLast = event == passport.events.last()
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.completeHandover(caseId)
                                onBackHome()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMPLETE CUSTODY TRANSFER & CLOSE CASE",
                                fontWeight = FontWeight.Bold,
                                color = DarkBg,
                                fontSize = 13.sp
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun HandoverCheckItem(check: HandoverCheck) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (check.passed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (check.passed) VerifiedGreen else TamperedRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = check.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = check.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
