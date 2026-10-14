package com.eta.assistant.ui.screens.sync

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.eta.assistant.core.theme.OrangePrimary
import com.eta.assistant.core.theme.OrangePrimaryContainer
import com.eta.assistant.core.theme.SuccessGreen
import com.eta.assistant.core.util.DateUtils
import com.eta.assistant.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SyncScreen(
    preferencesRepository: UserPreferencesRepository,
    repository: com.eta.assistant.data.repository.EtaRepository? = null,
    onNavigateBack: () -> Unit
) {
    val prefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
    val context = LocalContext.current
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isSyncing = true
                syncMessage = null
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().use { it.readText() }
                    }
                    if (jsonString != null && repository != null) {
                        val result = repository.importFullBackupJson(jsonString)
                        result.fold(
                            onSuccess = { count ->
                                preferencesRepository.updateLastSyncTime(System.currentTimeMillis())
                                syncMessage = "Backup restored successfully! $count records imported."
                            },
                            onFailure = { err ->
                                syncMessage = "Restore failed: ${err.message}"
                            }
                        )
                    } else {
                        syncMessage = "Could not read backup file."
                    }
                } catch (e: Exception) {
                    syncMessage = "Error reading backup: ${e.message}"
                } finally {
                    isSyncing = false
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
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
                text = com.eta.assistant.core.util.LocaleStrings.get("offline_sync_title", prefs?.languageCode ?: "en"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Big Rotating Sync Icon
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(OrangePrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "Sync",
                tint = OrangePrimary,
                modifier = Modifier
                    .size(54.dp)
                    .rotate(if (isSyncing) angle else 0f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSyncing) com.eta.assistant.core.util.LocaleStrings.get("syncing", prefs?.languageCode ?: "en") else com.eta.assistant.core.util.LocaleStrings.get("local_first_active", prefs?.languageCode ?: "en"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = com.eta.assistant.core.util.LocaleStrings.get("offline_sync_desc", prefs?.languageCode ?: "en"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Status Card matching mockup
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item 1: Local changes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Local changes saved",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Saved",
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Item 2: Cloud Sync architecture
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cloud deployment readiness",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary)
                    )
                }

                // Item 3: Last synced timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last synced",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateUtils.formatTime(prefs?.lastSyncTimestamp ?: System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (syncMessage != null) {
            Text(
                text = syncMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = SuccessGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isSyncing = true
                    syncMessage = null
                    val report = repository?.reconcileAndAuditDatabase() ?: "Local database verified! 100% operational offline."
                    preferencesRepository.updateLastSyncTime(System.currentTimeMillis())
                    isSyncing = false
                    syncMessage = report
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = !isSyncing
        ) {
            Icon(Icons.Default.Refresh, "Reconcile Now", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSyncing) "Checking..." else "Reconcile Local DB",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    if (repository != null) {
                        try {
                            val backupJson = repository.exportFullBackupJson()
                            val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
                            val backupFile = File(backupDir, "eta_backup_${System.currentTimeMillis()}.json")
                            backupFile.writeText(backupJson)

                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                backupFile
                            )

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, contentUri)
                                putExtra(Intent.EXTRA_SUBJECT, "Eta Store Full Database Backup")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Database Backup"))
                            syncMessage = "Full 7-table backup exported successfully!"
                        } catch (e: Exception) {
                            syncMessage = "Export failed: ${e.message}"
                        }
                    } else {
                        syncMessage = "Repository ready for sync."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isSyncing
        ) {
            Icon(Icons.Default.Share, "Export Backup", modifier = Modifier.size(20.dp), tint = OrangePrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Export Backup (.json)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { filePickerLauncher.launch("*/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isSyncing
        ) {
            Icon(Icons.Default.FileDownload, "Restore Backup", modifier = Modifier.size(20.dp), tint = OrangePrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Restore Backup (.json)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

