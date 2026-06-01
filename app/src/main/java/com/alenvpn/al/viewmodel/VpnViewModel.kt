// Copyright Alen Pepa

package com.alenvpn.al.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alenvpn.al.data.VpnRepository
import com.alenvpn.al.data.WireguardProfile
import com.alenvpn.al.services.WireGuardManager
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VpnViewModel(private val repository: VpnRepository) : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add("[$timestamp] $message")
        if (currentLogs.size > 50) currentLogs.removeAt(0)
        _logs.value = currentLogs
    }

    fun clearLogs() {
        _logs.value = emptyList()
        addLog("System logs cleared.")
    }

    val uiState: StateFlow<List<WireguardProfile>> = repository.allProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProfile(name: String, privateKey: String, address: String, dns: String, peerPublicKey: String, peerEndpoint: String, peerAllowedIps: String, persistentKeepalive: Int = 25, defaultMtu: Int = 1280, killSwitch: Boolean = false, autoConnect: Boolean = false, rawConfig: String = "") {
        addLog("Adding new profile: $name")
        viewModelScope.launch {
            repository.insertProfile(
                WireguardProfile(
                    name = name,
                    privateKey = privateKey,
                    address = address,
                    dns = dns,
                    peerPublicKey = peerPublicKey,
                    peerEndpoint = peerEndpoint,
                    peerAllowedIps = peerAllowedIps,
                    persistentKeepalive = persistentKeepalive,
                    defaultMtu = defaultMtu,
                    killSwitch = killSwitch,
                    autoConnect = autoConnect,
                    rawConfig = rawConfig
                )
            )
        }
    }

    fun parseConfigAndAdd(configText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Basic naive parser for WG Conf
            var name = "Imported Profile"
            var privateKey = ""
            var address = ""
            var dns = ""
            var peerPublicKey = ""
            var peerEndpoint = ""
            var peerAllowedIps = ""
            
            var persistentKeepalive = 25
            var defaultMtu = 1280
            var killSwitch = false
            var autoConnect = false
            
            val lines = configText.lines()
            for (line in lines) {
                val cleanStr = line.substringBefore("#").trim()
                if (cleanStr.startsWith("PrivateKey", ignoreCase = true)) {
                    privateKey = cleanStr.substringAfter("=").trim()
                } else if (cleanStr.startsWith("Address", ignoreCase = true)) {
                    address = cleanStr.substringAfter("=").trim()
                } else if (cleanStr.startsWith("DNS", ignoreCase = true)) {
                    dns = cleanStr.substringAfter("=").trim()
                } else if (cleanStr.startsWith("PublicKey", ignoreCase = true)) {
                    peerPublicKey = cleanStr.substringAfter("=").trim()
                } else if (cleanStr.startsWith("Endpoint", ignoreCase = true)) {
                    peerEndpoint = cleanStr.substringAfter("=").trim()
                } else if (cleanStr.startsWith("AllowedIPs", ignoreCase = true)) {
                    peerAllowedIps = cleanStr.substringAfter("=").trim()
                } else if (cleanStr.startsWith("PersistentKeepalive", ignoreCase = true)) {
                    persistentKeepalive = cleanStr.substringAfter("=").trim().toIntOrNull() ?: 25
                } else if (cleanStr.startsWith("MTU", ignoreCase = true)) {
                    defaultMtu = cleanStr.substringAfter("=").trim().toIntOrNull() ?: 1280
                }
            }
            
            withContext(Dispatchers.Main) {
                addProfile(name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps, persistentKeepalive, defaultMtu, killSwitch, autoConnect, configText)
            }
        }
    }

    fun deleteProfile(id: Int) {
        addLog("Deleting profile ID: $id")
        viewModelScope.launch {
            repository.deleteProfile(id)
            addLog("Profile $id deleted.")
        }
    }

    fun updateProfile(id: Int, name: String, privateKey: String, address: String, dns: String, peerPublicKey: String, peerEndpoint: String, peerAllowedIps: String, persistentKeepalive: Int, defaultMtu: Int, killSwitch: Boolean, autoConnect: Boolean, rawConfig: String) {
        addLog("Updating profile ID: $id ($name)")
        viewModelScope.launch {
            repository.updateProfile(
                WireguardProfile(
                    id = id,
                    name = name,
                    privateKey = privateKey,
                    address = address,
                    dns = dns,
                    peerPublicKey = peerPublicKey,
                    peerEndpoint = peerEndpoint,
                    peerAllowedIps = peerAllowedIps,
                    persistentKeepalive = persistentKeepalive,
                    defaultMtu = defaultMtu,
                    killSwitch = killSwitch,
                    autoConnect = autoConnect,
                    rawConfig = rawConfig
                )
            )
        }
    }

    fun connectToProfile(id: Int) {
        addLog("Attempting connection to profile ID $id")
        viewModelScope.launch {
            repository.connectToProfile(id)
            addLog("Connection request sent.")
        }
    }

    fun disconnect() {
        addLog("Disconnecting...")
        viewModelScope.launch {
            repository.disconnectAll()
            addLog("Disconnected.")
        }
    }

    suspend fun checkPing(host: String): Long = withContext(Dispatchers.IO) {
        try {
            val endpoint = host.substringBefore(":")
            if (endpoint.isEmpty()) return@withContext -1L
            val start = System.currentTimeMillis()
            val reachable = java.net.InetAddress.getByName(endpoint).isReachable(3000)
            if (reachable) System.currentTimeMillis() - start else -1L
        } catch (e: Throwable) {
            -1L
        }
    }

    suspend fun syncState() {
        val currentState = WireGuardManager.getState()
        if (currentState == Tunnel.State.DOWN) {
            disconnect()
        }
    }

    class Factory(private val repository: VpnRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VpnViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VpnViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
