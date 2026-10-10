package com.eta.assistant.ui.screens.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.OrangePrimaryContainer
import com.eta.assistant.core.theme.SuccessGreen
import com.eta.assistant.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

@Composable
fun PricingScreen(
    preferencesRepository: UserPreferencesRepository,
    onNavigateBack: () -> Unit
) {
    val prefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
    val currentPlan = prefs?.subscriptionPlan ?: "FREE"
    val scope = rememberCoroutineScope()

    var isAnnual by remember { mutableStateOf(false) }
    var planToUpgrade by remember { mutableStateOf<String?>(null) }
    var successDialogPlan by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar
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
                text = "Plans & Pricing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Supercharge Your Store",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pick the plan that fits your retail shop. Cancel anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Monthly / Annual billing toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (!isAnnual) OrangePrimary else Color.Transparent)
                        .clickable { isAnnual = false }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Monthly",
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isAnnual) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (isAnnual) OrangePrimary else Color.Transparent)
                        .clickable { isAnnual = true }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Annual",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAnnual) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Save 20%",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Plan 1: Free Starter Plan
        PricingPlanCard(
            title = "Free Starter",
            badge = null,
            price = "₹0",
            period = "forever",
            description = "Essential local-first offline ledger for single shop owners.",
            features = listOf(
                "1 Device (100% offline Room SQLite)",
                "Full product & stock management",
                "Customer dues ledger (Khata)",
                "Today's overview & basic reports",
                "Device-level voice commands"
            ),
            isCurrentPlan = currentPlan == "FREE",
            isPopular = false,
            ctaText = if (currentPlan == "FREE") "Active Plan" else "Downgrade to Free",
            onCtaClick = {
                scope.launch {
                    preferencesRepository.setSubscriptionPlan("FREE")
                    successDialogPlan = "Free Starter"
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Plan 2: Plus Plan (Most Popular)
        PricingPlanCard(
            title = "Eta Plus",
            badge = "MOST POPULAR",
            price = if (isAnnual) "₹1,999" else "₹199",
            period = if (isAnnual) "per year" else "per month",
            description = "Automated cloud backup, WhatsApp reminders, and advanced voice assistance.",
            features = listOf(
                "Everything in Free, plus:",
                "Automated Cloud Backup & Instant Restore",
                "WhatsApp Payment Reminders with 1-tap share",
                "Multilingual Assistant (Hindi, Tamil, Bengali)",
                "Push Notifications for low-stock warnings",
                "Priority email customer support"
            ),
            isCurrentPlan = currentPlan == "PLUS",
            isPopular = true,
            ctaText = if (currentPlan == "PLUS") "Active Plan" else "Upgrade to Plus",
            onCtaClick = { planToUpgrade = "PLUS" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Plan 3: Business Plan
        PricingPlanCard(
            title = "Eta Business",
            badge = "FOR GROWING TEAMS",
            price = if (isAnnual) "₹4,999" else "₹499",
            period = if (isAnnual) "per year" else "per month",
            description = "Multi-device syncing, staff logins, and Bluetooth receipt printing.",
            features = listOf(
                "Everything in Plus, plus:",
                "Multi-device & Staff logins (Owner + Cashier)",
                "Bluetooth Thermal Printer support (58mm/80mm)",
                "Tax & GST-ready CSV / Excel reports",
                "Profit & Loss margin analytics",
                "Dedicated WhatsApp & phone support"
            ),
            isCurrentPlan = currentPlan == "BUSINESS",
            isPopular = false,
            ctaText = if (currentPlan == "BUSINESS") "Active Plan" else "Upgrade to Business",
            onCtaClick = { planToUpgrade = "BUSINESS" }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Upgrade Confirmation Dialog
    if (planToUpgrade != null) {
        val planName = if (planToUpgrade == "PLUS") "Eta Plus" else "Eta Business"
        val priceStr = if (planToUpgrade == "PLUS") {
            if (isAnnual) "₹1,999 / year" else "₹199 / month"
        } else {
            if (isAnnual) "₹4,999 / year" else "₹499 / month"
        }

        AlertDialog(
            onDismissRequest = { planToUpgrade = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Confirm Subscription")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You are selecting $planName at $priceStr.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Instant activation\n• All premium features unlocked\n• Safe offline local sync preserved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = planToUpgrade ?: "FREE"
                        scope.launch {
                            preferencesRepository.setSubscriptionPlan(target)
                            planToUpgrade = null
                            successDialogPlan = planName
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Activate Plan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { planToUpgrade = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success dialog
    if (successDialogPlan != null) {
        AlertDialog(
            onDismissRequest = { successDialogPlan = null },
            title = { Text("Plan Updated! 🎉") },
            text = {
                Text("Your store is now on $successDialogPlan. All associated benefits are active.")
            },
            confirmButton = {
                Button(
                    onClick = { successDialogPlan = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Great!")
                }
            }
        )
    }
}

@Composable
private fun PricingPlanCard(
    title: String,
    badge: String?,
    price: String,
    period: String,
    description: String,
    features: List<String>,
    isCurrentPlan: Boolean,
    isPopular: Boolean,
    ctaText: String,
    onCtaClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isPopular) Modifier.border(2.dp, OrangePrimary, RoundedCornerShape(20.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPopular) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row with Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(OrangePrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isCurrentPlan) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CURRENT PLAN",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Row
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ $period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Feature list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(OrangePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // CTA Button
            Button(
                onClick = onCtaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isCurrentPlan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isPopular) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = ctaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

