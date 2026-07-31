package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
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
import com.example.ui.theme.EmeraldActive

@Composable
fun OTPBadge(
    otpCode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldActive.copy(alpha = 0.15f))
            .border(1.dp, EmeraldActive.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable { onCopyClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = "OTP Code",
            tint = EmeraldActive,
            modifier = Modifier.padding(2.dp)
        )
        Text(
            text = "OTP: $otpCode",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            color = EmeraldActive
        )
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy Code",
            tint = EmeraldActive,
            modifier = Modifier.padding(2.dp)
        )
    }
}
