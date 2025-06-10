package com.wizardlydo.app.screens.donation.comps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun QuickAmountButton(
    amount: String,
    onClick: (String) -> Unit
) {
    val displayText = if (amount == "Custom") "Custom" else "$$amount"
    val configuration = LocalConfiguration.current
    configuration.screenWidthDp.dp


    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick(amount) },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE6E6E6)
    ) {
        Box(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
