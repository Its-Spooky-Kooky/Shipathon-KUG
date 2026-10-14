package com.eta.assistant.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.SuccessGreen

object UpiHelper {
    fun buildUpiUri(upiId: String, businessName: String, amount: Double, note: String = "Store Bill"): String {
        val encodedName = java.net.URLEncoder.encode(businessName, "UTF-8")
        val encodedNote = java.net.URLEncoder.encode(note, "UTF-8")
        val formattedAmt = String.format(java.util.Locale.US, "%.2f", amount)
        return "upi://pay?pa=${upiId.trim()}&pn=$encodedName&am=$formattedAmt&cu=INR&tn=$encodedNote"
    }

    fun openUpiApp(context: Context, upiUri: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(upiUri)
        }
        val chooser = Intent.createChooser(intent, "Pay with UPI")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun StylizedQrPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val unit = w / 9f

        // Draw outer finder squares (top-left, top-right, bottom-left)
        fun drawFinder(x: Float, y: Float) {
            drawRect(
                color = Color.Black,
                topLeft = Offset(x, y),
                size = Size(unit * 3, unit * 3)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(x + unit * 0.5f, y + unit * 0.5f),
                size = Size(unit * 2, unit * 2)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(x + unit, y + unit),
                size = Size(unit, unit)
            )
        }

        drawFinder(0f, 0f)
        drawFinder(unit * 6, 0f)
        drawFinder(0f, unit * 6)

        // Draw decorative QR data modules
        val dots = listOf(
            4 to 0, 4 to 1, 4 to 2, 4 to 4,
            0 to 4, 1 to 4, 2 to 4,
            3 to 3, 5 to 3, 4 to 5,
            6 to 5, 7 to 4, 8 to 5, 5 to 7,
            7 to 7, 8 to 8, 5 to 8, 6 to 7
        )
        for ((dx, dy) in dots) {
            drawRect(
                color = Color.Black,
                topLeft = Offset(dx * unit, dy * unit),
                size = Size(unit * 0.9f, unit * 0.9f)
            )
        }
    }
}

@Composable
fun UpiPaymentCard(
    upiId: String,
    businessName: String,
    amount: Double,
    currencySymbol: String = "₹",
    languageCode: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val upiUri = UpiHelper.buildUpiUri(upiId, businessName, amount)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocaleStrings.get("upi_payment_qr", languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
                Text(
                    text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // QR Frame with Canvas pattern
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                StylizedQrPattern(modifier = Modifier.size(100.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "UPI ID: $upiId",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { UpiHelper.openUpiApp(context, upiUri) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(LocaleStrings.get("open_upi_verify", languageCode), fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Offline UPI: Records payment locally and launches GPay/PhonePe",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

