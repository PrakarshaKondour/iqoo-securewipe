package com.wipeproof.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wipeproof.app.core.model.VerificationStatus
import com.wipeproof.app.ui.theme.*

@Composable
fun StatusBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val (bgColor, textColor, text, icon) = when (status) {
        VerificationStatus.VERIFIED -> Quadruple(
            VerifiedGreenBg, VerifiedGreen, "VERIFIED ✓", Icons.Default.CheckCircle
        )
        VerificationStatus.TAMPERED -> Quadruple(
            TamperedRedBg, TamperedRed, "INVALID / TAMPERED ✕", Icons.Default.Close
        )
        VerificationStatus.INVALID -> Quadruple(
            TamperedRedBg, TamperedRed, "INVALID CERTIFICATE ✕", Icons.Default.Close
        )
        VerificationStatus.DEMO -> Quadruple(
            DemoAmberBg, DemoAmber, "DEMO MODE (SYNTHETIC)", Icons.Default.Warning
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = if (large) 16.dp else 10.dp, vertical = if (large) 8.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(if (large) 22.dp else 16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (large) 16.sp else 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
