package com.eta.assistant.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.SuccessGreen
import com.eta.assistant.core.util.LocaleStrings
import com.eta.assistant.core.util.NotificationHelper
import com.eta.assistant.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    onNavigateBack: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToPricing: () -> Unit
) {
    val prefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
    val lang = prefs?.languageCode ?: "en"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
                text = LocaleStrings.get("settings", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = OrangePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = prefs?.ownerName ?: "Ravi Kumar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${prefs?.businessName ?: "Ravi Store"} • ${LocaleStrings.get("offline", lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Settings items card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Plans & Pricing row
                SettingsRowItem(
                    icon = Icons.Default.Star,
                    title = "Plans & Pricing",
                    subtitle = "Current plan: ${prefs?.subscriptionPlan ?: "FREE"}",
                    onClick = onNavigateToPricing
                )

                SettingsRowItem(
                    icon = Icons.Default.Business,
                    title = LocaleStrings.get("business_info", lang),
                    subtitle = prefs?.businessCategory ?: "Groceries",
                    onClick = {}
                )

                SettingsRowItem(
                    icon = Icons.Default.CloudSync,
                    title = LocaleStrings.get("sync_backup", lang),
                    subtitle = "Room SQLite (Local-first)",
                    onClick = onNavigateToSync
                )

                // Language Option with active selection
                SettingsRowItem(
                    icon = Icons.Default.Language,
                    title = LocaleStrings.get("language", lang),
                    subtitle = LocaleStrings.getLanguageName(lang),
                    onClick = { showLanguageDialog = true }
                )

                // Theme Option
                SettingsRowItem(
                    icon = Icons.Default.DarkMode,
                    title = LocaleStrings.get("theme", lang),
                    subtitle = when (prefs?.themeMode) {
                        "DARK" -> "Dark (Night Mode)"
                        "LIGHT" -> "Light"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )

                // Notifications Option with active toggle and test
                SettingsRowItem(
                    icon = Icons.Default.Notifications,
                    title = LocaleStrings.get("notifications", lang),
                    subtitle = if (prefs?.notificationsEnabled != false)
                        LocaleStrings.get("notification_enabled", lang)
                    else
                        LocaleStrings.get("notification_disabled", lang),
                    onClick = { showNotificationDialog = true }
                )

                SettingsRowItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Help & Support",
                    onClick = {}
                )

                SettingsRowItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "v1.0.0 (Eta MVP)",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // 1. Language Selection Dialog (English, Hindi, Tamil, Bengali)
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentCode = lang,
            onDismiss = { showLanguageDialog = false },
            onSelect = { code ->
                scope.launch {
                    preferencesRepository.setLanguage(code)
                }
                showLanguageDialog = false
            }
        )
    }

    // 2. Notification Management Dialog
    if (showNotificationDialog) {
        NotificationSettingsDialog(
            currentLanguage = lang,
            isEnabled = prefs?.notificationsEnabled ?: true,
            onDismiss = { showNotificationDialog = false },
            onToggleEnabled = { enabled ->
                scope.launch {
                    preferencesRepository.setNotificationsEnabled(enabled)
                }
            },
            onSendTestNotification = {
                NotificationHelper.sendTestNotification(context, lang)
            }
        )
    }

    // 3. Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = prefs?.themeMode ?: "SYSTEM",
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                scope.launch {
                    preferencesRepository.setThemeMode(mode)
                }
                showThemeDialog = false
            }
        )
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = OrangePrimary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Language / भाषा चुनें",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LocaleStrings.supportedLanguages.forEach { appLang ->
                    val isSelected = currentCode == appLang.code
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(appLang.code) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = appLang.nativeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = appLang.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun NotificationSettingsDialog(
    currentLanguage: String,
    isEnabled: Boolean,
    onDismiss: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSendTestNotification: () -> Unit
) {
    var notificationSentMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleEnabled(true)
            onSendTestNotification()
            notificationSentMessage = "Permission granted! Test notification sent."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LocaleStrings.get("notifications", currentLanguage),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Stay updated on low stock alerts, pending customer dues, and daily summaries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onToggleEnabled(checked)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangePrimary
                        )
                    )
                }

                // Test Notification Button
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onSendTestNotification()
                        notificationSentMessage = "Test notification sent! Check your notification bar."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary.copy(alpha = 0.15f),
                        contentColor = OrangePrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = LocaleStrings.get("send_test_notification", currentLanguage),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (notificationSentMessage != null) {
                    Text(
                        text = notificationSentMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "SYSTEM" to "System default",
                    "LIGHT" to "Light (Orange & White)",
                    "DARK" to "Dark (Night Mode)"
                ).forEach { (mode, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentMode == mode) OrangePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentMode == mode) OrangePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Close")
            }
        }
    )
}
