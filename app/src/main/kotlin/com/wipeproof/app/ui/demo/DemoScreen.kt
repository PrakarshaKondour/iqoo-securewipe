package com.wipeproof.app.ui.demo

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wipeproof.app.core.crypto.CertificateBuilder
import com.wipeproof.app.core.crypto.CertificateVerifier
import com.wipeproof.app.core.crypto.CryptoEngine
import com.wipeproof.app.core.model.*
import com.wipeproof.app.demo.DemoOrchestrator
import com.wipeproof.app.ui.components.DemoBanner
import com.wipeproof.app.ui.components.QrCodeImage
import com.wipeproof.app.ui.scanner.VerificationResultCard
import com.wipeproof.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(
    onExitDemo: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cryptoEngine = remember { CryptoEngine() }
    val certBuilder = remember { CertificateBuilder(cryptoEngine) }
    val verifier = remember { CertificateVerifier(cryptoEngine, certBuilder) }

    var step by remember { mutableStateOf(1) }
    var signedCert by remember { mutableStateOf<SignedCertificate?>(null) }
    var validQrPayload by remember { mutableStateOf("") }
    var tamperedQrPayload by remember { mutableStateOf("") }
    var verificationResult by remember { mutableStateOf<VerificationResult?>(null) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Interactive Hackathon Demo", style = MaterialTheme.typography.titleMedium, color = DemoAmber)
                        Text("90-Second Verifiable Sanitization Journey", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExitDemo) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Demo", tint = TextPrimary)
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
            item { DemoBanner() }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DemoAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Demo Flow Stage: Step $step of 5",
                            fontWeight = FontWeight.Bold,
                            color = DemoAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val stageTitle = when (step) {
                            1 -> "1. Target Device Identification & Assessment (iQOO 12)"
                            2 -> "2. Sanitization Execution & Evidence Hashing"
                            3 -> "3. ECDSA Signed Certificate Issued"
                            4 -> "4. Second-Party Verification (Integrity Confirmed)"
                            else -> "5. Tamper Attack Simulation (Tamper Detected)"
                        }
                        Text(text = stageTitle, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }

            when (step) {
                1 -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Device: iQOO 12 (Snapdragon 8 Gen 3)", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Serial: IQOO-DEMO-2024-X99 • Asset: CORP-77291", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Assessed Storage:", fontWeight = FontWeight.SemiBold, color = PrimaryTeal, fontSize = 12.sp)
                                Text("• UFS 4.0 Internal (256 GB) -> FACTORY_RESET (Clear / FBE Crypto-Erase)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("• SanDisk Extreme SD (128 GB) -> SD_OVERWRITE_3PASS (Purge / NIST 800-88)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("RUN SANITIZATION & COLLECT PROOFS", fontWeight = FontWeight.Bold, color = DarkBg)
                        }
                    }
                }

                2 -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Sanitization Complete ✓", fontWeight = FontWeight.Bold, color = VerifiedGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Generated Technical Evidence:", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 12.sp)
                                Text("• Sector 1000000: a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("• Sector 2000000: e3b0c44298fc1c149afbf4c8996fb92427ae41e4", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("• TEE Key Invalidation: 5e884898da28047151d0e56f8dc6292773603d0d", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val demoCase = SanitizationCase(
                                        id = "demo-1",
                                        caseId = "WP-DEMO-IQOO-12",
                                        deviceInfo = DeviceInfo(
                                            manufacturer = "iQOO",
                                            model = "iQOO 12",
                                            serialNumber = "IQOO-DEMO-2024-X99",
                                            assetId = "CORP-77291",
                                            androidVersion = "Android 14",
                                            buildId = "V2307A",
                                            securityPatchLevel = "2024-08-01",
                                            storageDescriptors = emptyList(),
                                            fingerprint = "iqoo-fp-hash-test"
                                        ),
                                        status = CaseStatus.SANITIZED,
                                        selectedMethod = SanitizationMethod.DEMO,
                                        isDemoMode = true,
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )

                                    val cert = certBuilder.buildAndSign(
                                        case_ = demoCase,
                                        events = emptyList(),
                                        verificationOutcome = VerificationOutcome.DEMO,
                                        evidenceHashes = listOf(
                                            EvidenceHashPayload("Demo Sector Sample", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", "Audit sample")
                                        )
                                    )
                                    signedCert = cert
                                    validQrPayload = certBuilder.encodeToQrPayload(cert)
                                    step = 3
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("SIGN CERTIFICATE WITH HARDWARE KEY", fontWeight = FontWeight.Bold, color = DarkBg)
                        }
                    }
                }

                3 -> {
                    item {
                        Text(
                            text = "Signed Certificate QR Generated",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QrCodeImage(content = validQrPayload, size = 220.dp)
                    }

                    item {
                        Button(
                            onClick = {
                                verificationResult = verifier.verifyQrPayload(validQrPayload)
                                step = 4
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SCAN AS 2ND PARTY (VERIFY)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                4 -> {
                    verificationResult?.let { res ->
                        item {
                            VerificationResultCard(result = res)
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                signedCert?.let { orig ->
                                    val tampered = orig.certificate.copy(
                                        deviceModel = "iQOO 12 [TAMPERED_IN_TRANSIT]",
                                        verificationOutcome = "FORGED_RECORD"
                                    )
                                    val tamperedSigned = SignedCertificate(
                                        certificate = tampered,
                                        signatureBase64 = orig.signatureBase64 // Kept original signature!
                                    )
                                    tamperedQrPayload = certBuilder.encodeToQrPayload(tamperedSigned)
                                    verificationResult = verifier.verifyQrPayload(tamperedQrPayload)
                                    step = 5
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TamperedRed)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TAMPER WITH CERTIFICATE & RE-VERIFY", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                5 -> {
                    verificationResult?.let { res ->
                        item {
                            VerificationResultCard(result = res)
                        }
                    }

                    item {
                        Button(
                            onClick = { step = 1 },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESTART DEMO", fontWeight = FontWeight.Bold, color = DarkBg)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
