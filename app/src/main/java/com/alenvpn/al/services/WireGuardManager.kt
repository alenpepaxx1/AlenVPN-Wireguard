package com.alenvpn.al.services

import android.content.Context
import android.util.Log
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.Statistics
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

object WireGuardManager {
    private var backend: Backend? = null
    private val tunnel = object : Tunnel {
        override fun getName() = "wg0"
        override fun onStateChange(newState: Tunnel.State) {
            Log.d("WireGuardManager", "State changed: $newState")
        }
    }

    fun init(context: Context) {
        if (backend == null) {
            try {
                backend = GoBackend(context.applicationContext)
                Log.d("WireGuardManager", "GoBackend initialized")
            } catch (e: Throwable) {
                Log.e("WireGuardManager", "Failed to init GoBackend", e)
            }
        }
    }

    suspend fun connect(wgConfig: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("WireGuardManager", "Connecting...")
            val config = Config.parse(ByteArrayInputStream(wgConfig.toByteArray()))
            if (backend == null) throw Exception("VPN Backend is not initialized. Try restarting the app.")
            backend?.setState(tunnel, Tunnel.State.UP, config)
            Log.d("WireGuardManager", "Connected successfully")
        } catch (e: Exception) {
            Log.e("WireGuardManager", "Connect failed", e)
            throw e
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            Log.d("WireGuardManager", "Disconnecting...")
            backend?.setState(tunnel, Tunnel.State.DOWN, null)
            Log.d("WireGuardManager", "Disconnected successfully")
        } catch (e: Exception) {
            Log.e("WireGuardManager", "Disconnect failed", e)
        }
    }

    suspend fun getStatistics(): Pair<Long, Long> = withContext(Dispatchers.IO) {
        try {
            val stats = backend?.getStatistics(tunnel)
            val rx = stats?.totalRx() ?: 0L
            val tx = stats?.totalTx() ?: 0L
            Pair(rx, tx)
        } catch (e: Throwable) {
            Log.e("WireGuardManager", "Sync stats failed", e)
            Pair(0L, 0L)
        }
    }

    suspend fun getState(): Tunnel.State = withContext(Dispatchers.IO) {
        try {
            backend?.getState(tunnel) ?: Tunnel.State.DOWN
        } catch (e: Throwable) {
            Log.e("WireGuardManager", "Get state failed", e)
            Tunnel.State.DOWN
        }
    }
}
