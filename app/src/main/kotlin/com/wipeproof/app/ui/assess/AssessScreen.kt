package com.wipeproof.app.ui.assess

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.core.sanitization.MethodAssessment
import com.wipeproof.app.ui.components.DemoBanner
import com.wipeproof.app.ui.components.MethodBadge
import com.wipeproof.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessScreen(
    caseId: String,
    onProceedToSanitize: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: AssessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(caseId) {
        viewModel.loadAssessment(caseId)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Step 2 of 6: ASSESS", style = MaterialTheme.typography.titleMedium, color = PrimaryTeal)
                        Text("Sanitization Method Assessment", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
            is AssessUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
            is AssessUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TamperedRed)
                }
            }
            is AssessUiState.Success -> {
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Target Device Assessment",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${state.case_.deviceInfo.manufacturer} ${state.case_.deviceInfo.model} • Case ${state.case_.caseId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "ASSESSED STORAGE & RECOMMENDED MECHANISMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(state.assessments) { assessment ->
                        AssessmentCard(assessment)
                    }

                    item {
                        // Plain-language rule-based explanation card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, PrimaryTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Technical & OS Integrity Principle",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryTeal,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "WipeProof strictly uses technically valid sanitization methods. On modern Android, application sandboxes cannot overwrite internal eMMC/UFS flash directly. The genuine sanitization mechanism is OS Factory Reset with cryptographic erase (TEE Keystore key destruction), meeting NIST SP 800-88 Clear. Any app claiming to overwrite raw Android internal flash is deceptive.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onProceedToSanitize(state.case_.caseId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text(
                                text = "PROCEED TO GUIDED SANITIZATION",
                                fontWeight = FontWeight.Bold,
                                color = DarkBg,
                                fontSize = 14.sp
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
fun AssessmentCard(assessment: MethodAssessment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assessment.storageDescriptor.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                MethodBadge(classification = assessment.recommendedMethod.classification)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Method: ${assessment.recommendedMethod.displayName}",
                fontWeight = FontWeight.SemiBold,
                color = PrimaryTeal,
                fontSize = 13.sp
            )

            assessment.recommendedMethod.nistReference?.let { nist ->
                Text(
                    text = nist,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            for (note in assessment.notes) {
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "• ", color = TextSecondary, fontSize = 12.sp)
                    Text(text = note, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}
