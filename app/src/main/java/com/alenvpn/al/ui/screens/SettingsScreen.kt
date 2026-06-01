// Copyright Alen Pepa

package com.alenvpn.al.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alenvpn.al.data.AppSettings
import com.alenvpn.al.ui.tr
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onViewLogs: () -> Unit
) {
    val context = LocalContext.current
    val killSwitchEnabled by AppSettings.killSwitch.collectAsStateWithLifecycle()
    val autoConnectEnabled by AppSettings.autoConnect.collectAsStateWithLifecycle()
    var keepAliveEnabled by remember { mutableStateOf(true) }
    val selectedTheme by AppSettings.theme.collectAsStateWithLifecycle()
    val selectedLanguage by AppSettings.language.collectAsStateWithLifecycle()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    val languages = listOf("en" to "English", "de" to "German", "it" to "Italian", "fr" to "French", "ru" to "Russian", "zh" to "Chinese", "sq" to "Albanian")
    val currentLanguageName = languages.find { it.first == selectedLanguage }?.second ?: "English"

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        TextButton(
                            onClick = { 
                                AppSettings.setLanguage(code)
                                showLanguageDialog = false 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name, color = if (code == selectedLanguage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Settings"), fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = tr("Connection")) {
                SettingsItem(
                    title = tr("Kill Switch"),
                    subtitle = "Block internet when VPN is disconnected",
                    icon = Icons.Filled.Lock,
                    trailing = {
                        Switch(checked = killSwitchEnabled, onCheckedChange = { AppSettings.setKillSwitch(it) })
                    },
                    onClick = { AppSettings.setKillSwitch(!killSwitchEnabled) }
                )
                SettingsItem(
                    title = tr("Auto Connect"),
                    subtitle = "Connect automatically on boot",
                    icon = Icons.Filled.Settings,
                    trailing = {
                        Switch(checked = autoConnectEnabled, onCheckedChange = { AppSettings.setAutoConnect(it) })
                    },
                    onClick = { AppSettings.setAutoConnect(!autoConnectEnabled) }
                )
                SettingsItem(
                    title = tr("Persistent Keepalive"),
                    subtitle = "Send periodic packets to keep tunnel active",
                    icon = Icons.Filled.Info,
                    trailing = {
                        Switch(checked = keepAliveEnabled, onCheckedChange = { keepAliveEnabled = it })
                    },
                    onClick = { keepAliveEnabled = !keepAliveEnabled }
                )
                SettingsItem(
                    title = tr("Default MTU"),
                    subtitle = "Current: 1280",
                    icon = Icons.Filled.Settings,
                    onClick = { /* Implement later */ }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = tr("Appearance")) {
                SettingsItem(
                    title = tr("Theme"),
                    subtitle = "Current: $selectedTheme",
                    icon = Icons.Filled.Info,
                    onClick = { 
                        val newTheme = when(selectedTheme) {
                            "System" -> "Dark"
                            "Dark" -> "Light"
                            else -> "System"
                        }
                        AppSettings.setTheme(newTheme)
                    }
                )
                SettingsItem(
                    title = tr("Language"),
                    subtitle = currentLanguageName,
                    icon = Icons.Filled.Lock,
                    onClick = { showLanguageDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = tr("General")) {
                SettingsItem(
                    title = tr("System Logs"),
                    subtitle = "View connection events",
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = onViewLogs
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = tr("About")) {
                SettingsItem(
                    title = "Version",
                    subtitle = "1.1.0 (Pro Features Enabled)",
                    icon = Icons.Filled.Info,
                    onClick = {}
                )
                SettingsItem(
                    title = tr("Check for Updates"),
                    subtitle = "App is up to date",
                    icon = Icons.Filled.Settings,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/alenpepaxx1"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = false, onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}
