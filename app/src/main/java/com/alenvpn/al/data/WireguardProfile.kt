// Copyright Alen Pepa

package com.alenvpn.al.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wireguard_profiles")
data class WireguardProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val privateKey: String,
    val address: String,
    val dns: String,
    val peerPublicKey: String,
    val peerEndpoint: String,
    val peerAllowedIps: String,
    val persistentKeepalive: Int = 25,
    val defaultMtu: Int = 1280,
    val killSwitch: Boolean = false,
    val autoConnect: Boolean = false,
    val rawConfig: String = "",
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
