package com.eta.assistant.ui.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.OrangePrimaryContainer
import com.eta.assistant.core.theme.SuccessGreen

@Composable
fun VoiceSaleScreen(
    viewModel: VoiceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToManualSale: () -> Unit,
    onSaleRecorded: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var typedCommand by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Audio Waveform animation simulation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = com.eta.assistant.core.util.LocaleStrings.get("voice_billing", state.languageCode),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Waveform indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp)
        ) {
            val barHeights = listOf(14, 24, 38, 20, 32, 16, 28, 36, 18, 26, 12)
            barHeights.forEach { height ->
                val dynamicHeight = if (state.isListening) (height * pulseScale).dp else (height * 0.4f).dp
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(dynamicHeight)
                        .clip(CircleShape)
                        .background(if (state.isListening) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Big Mic Button with Orange Ring
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(if (state.isListening) pulseScale else 1f)
                .clip(CircleShape)
                .background(if (state.isListening) OrangePrimary else OrangePrimaryContainer)
                .clickable {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPerm) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        if (state.isListening) viewModel.stopListening() else viewModel.startListening()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Microphone",
                tint = if (state.isListening) Color.White else OrangePrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (state.isListening) com.eta.assistant.core.util.LocaleStrings.get("listening", state.languageCode) else com.eta.assistant.core.util.LocaleStrings.get("tap_to_speak", state.languageCode),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = com.eta.assistant.core.util.LocaleStrings.get("voice_example_prompt", state.languageCode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Manual text input fallback
        OutlinedTextField(
            value = typedCommand,
            onValueChange = { typedCommand = it },
            placeholder = { Text(com.eta.assistant.core.util.LocaleStrings.get("type_command_hint", state.languageCode)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (typedCommand.isNotBlank()) {
                            viewModel.processCommand(typedCommand)
                            typedCommand = ""
                        }
                    }
                ) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Send, "Send", tint = OrangePrimary)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick noisy counter tap buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("₹500 Ramesh", "Add 5kg Rice", "Show Dues").forEach { quickQuery ->
                androidx.compose.material3.AssistChip(
                    onClick = {
                        val fullCommand = when (quickQuery) {
                            "₹500 Ramesh" -> "Record a ₹500 sale for Ramesh"
                            "Add 5kg Rice" -> "Add 5kg rice to stock"
                            else -> "Show my dues"
                        }
                        viewModel.processCommand(fullCommand)
                    },
                    label = { Text(quickQuery, fontSize = 12.sp) },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = OrangePrimaryContainer.copy(alpha = 0.5f),
                        labelColor = OrangePrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToManualSale,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ShoppingCart,
                contentDescription = "Manual Cart",
                modifier = Modifier.size(18.dp),
                tint = OrangePrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = com.eta.assistant.core.util.LocaleStrings.get("prefer_manual_cart", state.languageCode),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Parsed Action Card
        if (state.parsedIntent != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = com.eta.assistant.core.util.LocaleStrings.get("parsed_action", state.languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val summary = when (val intent = state.parsedIntent) {
                        is ParsedIntent.RecordSale -> "Record sale of ${intent.quantity.toInt()} ${intent.productName} for ${intent.customerName} (${intent.amount?.let { "₹$it" } ?: "Inventory Price"})"
                        is ParsedIntent.AddStock -> "Add ${intent.quantity.toInt()} ${intent.productName} to inventory stock"
                        is ParsedIntent.ShowDues -> "View customer outstanding balances"
                        is ParsedIntent.ShowTopSelling -> "View top selling items report"
                        is ParsedIntent.Unknown -> "Unknown command: \"${intent.rawText}\""
                        null -> ""
                    }

                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.executeParsedAction(
                                onNavigateToCustomers = onNavigateToCustomers,
                                onNavigateToReports = onNavigateToReports,
                                onSaleRecorded = onSaleRecorded
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = com.eta.assistant.core.util.LocaleStrings.get("confirm_action", state.languageCode), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.statusMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = state.statusMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = if (state.isSuccess) SuccessGreen else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Recent Commands section from mockup
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = com.eta.assistant.core.util.LocaleStrings.get("recent_commands", state.languageCode),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                val examples = listOf(
                    "Record a ₹500 sale for Ravi",
                    "Add 5kg rice to stock",
                    "Show my dues",
                    "What are my top selling items?"
                )

                examples.forEach { example ->
                    Text(
                        text = "• \"$example\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.processCommand(example) }
                            .padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

