package com.wipeproof.app.ui.prove

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.ui.components.DemoBanner
import com.wipeproof.app.ui.components.QrCodeImage
import com.wipeproof.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveScreen(
    caseId: String,
    onProceedToHandover: (String) -> Unit,
    onOpenVerifier: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProveViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(caseId) {
        viewModel.loadCertificate(caseId)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Step 5 of 6: PROVE", style = MaterialTheme.typography.titleMedium, color = PrimaryTeal)
                        Text("Signed Cryptographic Certificate", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
            is ProveUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
            is ProveUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TamperedRed)
                }
            }
            is ProveUiState.Ready -> {
                val cert = state.signedCertificate.certificate
                val issuedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(cert.issuedAt))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (cert.isDemoMode) {
                        item { DemoBanner() }
                    }

                    item {
                        // QR Code Presentation
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Scan to Verify Independently",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use another phone's WipeProof scanner to verify the signature",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            QrCodeImage(content = state.qrPayload, size = 260.dp)

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Case ID: ${cert.caseId}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal,
                                fontSize = 14.sp
                            )
                        }
                    }

                    item {
                        // Certificate Metadata Summary
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Certificate Specifications",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = DarkBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                CertRow("Algorithm", "ECDSA P-256 (secp256r1) + SHA-256")
                                CertRow("Device", "${cert.deviceManufacturer} ${cert.deviceModel}")
                                CertRow("Method", cert.methodName)
                                CertRow("Classification", cert.methodClassification)
                                CertRow("Issued Timestamp", issuedDate)
                                CertRow("Evidence Count", "${cert.evidenceHashes.size} Hash Proofs")
                                CertRow("Signature", "${state.signedCertificate.signatureBase64.take(16)}...")
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, state.qrPayload)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share WipeProof Certificate")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryTeal)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", color = PrimaryTeal)
                            }

                            Button(
                                onClick = onOpenVerifier,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Verifier", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onProceedToHandover(cert.caseId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text(
                                text = "VIEW SANITIZATION PASSPORT & HANDOVER",
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
fun CertRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
