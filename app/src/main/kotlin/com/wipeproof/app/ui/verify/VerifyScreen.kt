package com.wipeproof.app.ui.verify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.core.model.VerificationOutcome
import com.wipeproof.app.ui.components.DemoBanner
import com.wipeproof.app.ui.components.EvidenceCard
import com.wipeproof.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    caseId: String,
    onGenerateCertificate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: VerifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(caseId) {
        viewModel.loadVerification(caseId)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Step 4 of 6: VERIFY", style = MaterialTheme.typography.titleMedium, color = PrimaryTeal)
                        Text("Independent Evidence Verification", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
        when (val state = uiState) {
            is VerifyUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
            is VerifyUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TamperedRed)
                }
            }
            is VerifyUiState.Display -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.case_.isDemoMode) {
                        item { DemoBanner() }
                    }

                    item {
                        // Verification Status Header Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, VerifiedGreen, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Verification Outcome",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    val (badgeText, badgeColor) = when (state.outcome) {
                                        VerificationOutcome.SANITIZED_VERIFIED -> Pair("VERIFICATION PASSED", VerifiedGreen)
                                        VerificationOutcome.SANITIZED_UNVERIFIED -> Pair("UNVERIFIED", TamperedRed)
                                        VerificationOutcome.DEMO -> Pair("DEMO AUDIT PASS", DemoAmber)
                                        else -> Pair("FAILED", TamperedRed)
                                    }
                                    Text(
                                        text = badgeText,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Device: ${state.case_.deviceInfo.manufacturer} ${state.case_.deviceInfo.model} (${state.case_.caseId})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Method: ${state.case_.selectedMethod?.displayName ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "TECHNICAL EVIDENCE PACKAGE (${state.evidenceList.size} PROOFS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(state.evidenceList) { evidence ->
                        EvidenceCard(evidence = evidence)
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.generateCertificate { cId ->
                                    onGenerateCertificate(cId)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ISSUE CRYPTOGRAPHIC PROOF & CERTIFICATE",
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
