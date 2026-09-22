package com.wipeproof.app.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.core.model.VerificationResult
import com.wipeproof.app.core.model.VerificationStatus
import com.wipeproof.app.ui.components.CameraQrScanner
import com.wipeproof.app.ui.components.StatusBadge
import com.wipeproof.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("2nd-Party Certificate Verifier", style = MaterialTheme.typography.titleMedium, color = VerifiedGreen)
                        Text("Independent Offline Verification", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is ScannerUiState.Idle -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Second-Party Independent Audit",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Scan the QR certificate displayed on the sanitizing device. Verification runs locally in-process without network trust.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    item {
                        CameraQrScanner(
                            onQrCodeDetected = { raw ->
                                viewModel.verifyPayload(raw)
                            },
                            scanInstruction = "Point camera at WipeProof Certificate QR"
                        )
                    }
                }

                is ScannerUiState.Verifying -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryTeal)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Evaluating ECDSA P-256 Digital Signature...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                is ScannerUiState.Result -> {
                    item {
                        VerificationResultCard(result = state.verificationResult)
                    }

                    item {
                        Button(
                            onClick = { viewModel.resetScanner() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SCAN ANOTHER CERTIFICATE",
                                fontWeight = FontWeight.Bold,
                                color = DarkBg
                            )
                        }
                    }
                }

                is ScannerUiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TamperedRed, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Verification Error", fontWeight = FontWeight.Bold, color = TamperedRed)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(state.message, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item {
                        Button(onClick = { viewModel.resetScanner() }) {
                            Text("Try Again")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun VerificationResultCard(result: VerificationResult) {
    val isVerified = result.status == VerificationStatus.VERIFIED || result.status == VerificationStatus.DEMO
    val borderColor = if (isVerified) VerifiedGreen else TamperedRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusBadge(status = result.status, large = true)

            Spacer(modifier = Modifier.height(16.dp))

            if (isVerified) {
                Text(
                    text = "Cryptographic Seal Intact",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VerifiedGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The certificate payload matches the ECDSA P-256 digital signature. No tampering detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            } else {
                Text(
                    text = "TAMPER DETECTED / INVALID",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TamperedRed
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = result.failureReason ?: "Signature does not match payload. The certificate content was modified.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TamperedRed,
                    lineHeight = 20.sp
                )
                if (result.failedField != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Failed Field / Attribute: ${result.failedField}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(modifier = Modifier.height(16.dp))

            result.caseId?.let {
                AuditRow("Case ID", it)
            }
            result.method?.let {
                AuditRow("Sanitization Method", it)
            }
            result.deviceFingerprint?.let {
                AuditRow("Hardware Fingerprint", "${it.take(16)}...")
            }
            AuditRow("Signature Math Valid", if (result.signatureValid) "TRUE (ECDSA Verified)" else "FALSE (Mismatch)")
            AuditRow("Chain Integrity Valid", if (result.chainIntegrityValid) "TRUE (SHA-256 Intact)" else "FALSE")
            result.issuedAt?.let {
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(it))
                AuditRow("Issued Timestamp", dateStr)
            }
        }
    }
}

@Composable
fun AuditRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
