package com.wipeproof.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wipeproof.app.core.model.EventType
import com.wipeproof.app.core.model.SanitizationEvent
import com.wipeproof.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineItem(
    event: SanitizationEvent,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(event.timestamp))

    val (nodeColor, icon) = when (event.type) {
        EventType.CERTIFICATE_GENERATED, EventType.CERTIFICATE_VERIFIED -> Pair(PrimaryTeal, Icons.Default.Security)
        EventType.SANITIZATION_COMPLETED, EventType.VERIFICATION_COMPLETED -> Pair(VerifiedGreen, Icons.Default.CheckCircle)
        EventType.SANITIZATION_FAILED, EventType.VERIFICATION_FAILED -> Pair(TamperedRed, Icons.Default.Info)
        else -> Pair(TextSecondary, Icons.Default.Info)
    }

    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(nodeColor.copy(alpha = 0.2f))
                    .border(2.dp, nodeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = nodeColor,
                    modifier = Modifier.size(10.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(DarkBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hash: ${event.hash.take(16)}...",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = PrimaryTealDim
            )
        }
    }
}
