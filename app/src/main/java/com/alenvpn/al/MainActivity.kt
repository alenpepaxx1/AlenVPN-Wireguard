// Copyright Alen Pepa

package com.alenvpn.al

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alenvpn.al.data.VpnDatabase
import com.alenvpn.al.data.VpnRepository
import com.alenvpn.al.ui.screens.AddProfileScreen
import com.alenvpn.al.ui.screens.EditProfileScreen
import com.alenvpn.al.ui.screens.LogsScreen
import com.alenvpn.al.ui.screens.SettingsScreen
import com.alenvpn.al.ui.screens.HomeScreen
import com.alenvpn.al.ui.screens.SpeedTestScreen
import com.alenvpn.al.ui.theme.MyApplicationTheme
import com.alenvpn.al.viewmodel.VpnViewModel
import android.content.Intent
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.alenvpn.al.services.WireGuardManager
import com.alenvpn.al.data.WireguardProfile
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WireGuardManager.init(this)
        com.alenvpn.al.data.AppSettings.init(this)
        
        enableEdgeToEdge()
        
        val database = VpnDatabase.getDatabase(this)
        val repository = VpnRepository(database.vpnDao())
        
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeValue by com.alenvpn.al.data.AppSettings.theme.collectAsStateWithLifecycle()
            val darkTheme = when (themeValue) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VpnAppNavigation(repository, windowSizeClass)
                }
            }
        }
    }
}

@Composable
fun VpnAppNavigation(repository: VpnRepository, windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val viewModel: VpnViewModel = viewModel(factory = VpnViewModel.Factory(repository))
    val profiles by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.syncState()
    }
    
    var isConnecting by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(profiles.isNotEmpty()) {
        if (profiles.isNotEmpty()) {
            val currentState = WireGuardManager.getState()
            if (currentState == com.wireguard.android.backend.Tunnel.State.DOWN && com.alenvpn.al.data.AppSettings.autoConnect.value) {
                val autoProfile = profiles.find { it.autoConnect } ?: profiles.firstOrNull()
                if (autoProfile != null && !isConnecting) {
                    val wgConfig = if (autoProfile.rawConfig.isNotBlank()) autoProfile.rawConfig else buildString {
                        appendLine("[Interface]")
                        appendLine("PrivateKey = ${autoProfile.privateKey}")
                        appendLine("Address = ${if (autoProfile.address.contains("/")) autoProfile.address else autoProfile.address + "/32"}")
                        if (autoProfile.dns.isNotBlank()) appendLine("DNS = ${autoProfile.dns}")
                        else appendLine("DNS = 1.1.1.1, 8.8.8.8, 8.8.4.4")
                        appendLine("MTU = ${autoProfile.defaultMtu}")
                        appendLine()
                        appendLine("[Peer]")
                        appendLine("PublicKey = ${autoProfile.peerPublicKey}")
                        if (autoProfile.peerEndpoint.isNotBlank()) appendLine("Endpoint = ${autoProfile.peerEndpoint}")
                        appendLine("PersistentKeepalive = ${autoProfile.persistentKeepalive}")
                        appendLine("AllowedIPs = ${if (autoProfile.peerAllowedIps.isNotBlank()) autoProfile.peerAllowedIps else "0.0.0.0/0, ::/0"}")
                    }
                    scope.launch {
                        try {
                            isConnecting = true
                            WireGuardManager.connect(wgConfig)
                            viewModel.connectToProfile(autoProfile.id)
                        } catch (e: Exception) {
                            Log.e("VPN", "Auto connect failed", e)
                        } finally {
                            isConnecting = false
                        }
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                profiles = profiles,
                onAddProfileClick = { navController.navigate("add_profile") },
                onEditProfileClick = { profile -> navController.navigate("edit_profile/${profile.id}") },
                onSettingsClick = { navController.navigate("settings") },
                onConnectClick = { profile -> 
                    if (!isConnecting) {
                        val wgConfig = if (profile.rawConfig.isNotBlank()) {
                            profile.rawConfig
                        } else {
                            buildString {
                                appendLine("[Interface]")
                                appendLine("PrivateKey = ${profile.privateKey}")
                                appendLine("Address = ${if (profile.address.contains("/")) profile.address else profile.address + "/32"}")
                                if (profile.dns.isNotBlank()) {
                                    appendLine("DNS = ${profile.dns}")
                                } else {
                                    appendLine("DNS = 1.1.1.1, 8.8.8.8, 8.8.4.4")
                                }
                                appendLine("MTU = ${profile.defaultMtu}")
                                appendLine()
                                appendLine("[Peer]")
                                appendLine("PublicKey = ${profile.peerPublicKey}")
                                if (profile.peerEndpoint.isNotBlank()) {
                                    appendLine("Endpoint = ${profile.peerEndpoint}")
                                }
                                appendLine("PersistentKeepalive = ${profile.persistentKeepalive}")
                                appendLine("AllowedIPs = ${if (profile.peerAllowedIps.isNotBlank()) profile.peerAllowedIps else "0.0.0.0/0, ::/0"}")
                            }
                        }
                        
                        scope.launch {
                            try {
                                isConnecting = true
                                viewModel.addLog("Initializing WireGuard tunnel...")
                                WireGuardManager.connect(wgConfig)
                                viewModel.connectToProfile(profile.id)
                                viewModel.addLog("Tunnel established.")
                            } catch (e: Throwable) {
                                val errorMsg = e.message ?: e.toString()
                                viewModel.addLog("Connection failed: $errorMsg")
                                android.widget.Toast.makeText(context, "Connection failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                                viewModel.disconnect()
                            } finally {
                                isConnecting = false
                            }
                        }
                    }
                },
                onDisconnectClick = { 
                    scope.launch {
                        try {
                            WireGuardManager.disconnect()
                        } catch (e: Throwable) {
                            Log.e("MainActivity", "Disconnect error", e)
                        } finally {
                            viewModel.disconnect()
                        }
                    }
                },
                onDeleteClick = { id -> viewModel.deleteProfile(id) },
                onSpeedTestClick = { navController.navigate("speed_test") },
                viewModel = viewModel
            )
        }
        composable("speed_test") {
            SpeedTestScreen(onBack = { navController.popBackStack() })
        }
        composable("add_profile") {
            AddProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveProfile = { name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps ->
                    viewModel.addProfile(name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps)
                    navController.popBackStack()
                },
                onImportConfig = { configText ->
                    viewModel.parseConfigAndAdd(configText)
                    navController.popBackStack()
                }
            )
        }
        composable("edit_profile/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")?.toIntOrNull()
            val profile = profiles.find { it.id == profileId }
            if (profile != null) {
                EditProfileScreen(
                    profile = profile,
                    onNavigateBack = { navController.popBackStack() },
                    onUpdateProfile = { id, name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps, persistentKeepalive, defaultMtu, killSwitch, autoConnect, rawConfig ->
                        viewModel.updateProfile(id, name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps, persistentKeepalive, defaultMtu, killSwitch, autoConnect, rawConfig)
                        navController.popBackStack()
                    }
                )
            }
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewLogs = { navController.navigate("logs") }
            )
        }
        composable("logs") {
            val logs by viewModel.logs.collectAsStateWithLifecycle()
            LogsScreen(
                logs = logs,
                onNavigateBack = { navController.popBackStack() },
                onClearLogs = { viewModel.clearLogs() }
            )
        }
    }
}
