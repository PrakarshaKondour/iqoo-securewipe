package com.wipeproof.app.ui.identify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wipeproof.app.core.model.DeviceInfo
import com.wipeproof.app.ui.components.CameraQrScanner
import com.wipeproof.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifyScreen(
    onCaseCreated: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: IdentifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Step 1 of 6: IDENTIFY", style = MaterialTheme.typography.titleMedium, color = PrimaryTeal)
                        Text("Device & Storage Discovery", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
            item {
                Text(
                    text = "Scan Device Asset Label / Barcode",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CameraQrScanner(
                    onQrCodeDetected = { code ->
                        viewModel.onBarcodeScanned(code)
                    },
                    scanInstruction = "Scan physical asset barcode or QR tag (or paste asset ID)"
                )
            }

            when (val state = uiState) {
                is IdentifyUiState.Identified -> {
                    item {
                        DeviceCard(deviceInfo = state.deviceInfo)
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.createCase(isDemoMode = false) { caseId ->
                                    onCaseCreated(caseId)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text(
                                text = "PROCEED TO SANITIZATION ASSESSMENT",
                                fontWeight = FontWeight.Bold,
                                color = DarkBg,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is IdentifyUiState.Error -> {
                    item {
                        Text(
                            text = "Error: ${state.message}",
                            color = TamperedRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                IdentifyUiState.Idle -> {
                    item {
                        CircularProgressIndicator(color = PrimaryTeal)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DeviceCard(deviceInfo: DeviceInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${deviceInfo.manufacturer} ${deviceInfo.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(modifier = Modifier.height(10.dp))

            InfoRow("Serial Number", deviceInfo.serialNumber ?: "Restricted / Protected")
            if (deviceInfo.assetId != null) {
                InfoRow("Scanned Asset ID", deviceInfo.assetId)
            }
            InfoRow("OS Version", deviceInfo.androidVersion ?: "Unknown")
            InfoRow("Build ID", deviceInfo.buildId ?: "Unknown")
            InfoRow("Security Patch", deviceInfo.securityPatchLevel ?: "Unknown")

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ATTACHED STORAGE MEDIA (${deviceInfo.storageDescriptors.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            for (storage in deviceInfo.storageDescriptors) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBg)
                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = storage.label,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        val sizeText = storage.totalBytes?.let { "${it / (1024 * 1024 * 1024)} GB" } ?: "Capacity detected by OS"
                        Text(
                            text = "${storage.type.name} • $sizeText • Encrypted: ${storage.isEncrypted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hardware Fingerprint: ${deviceInfo.fingerprint.take(20)}...",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
