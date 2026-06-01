package com.alenvpn.al.data

import kotlinx.coroutines.flow.Flow

class VpnRepository(private val vpnDao: VpnDao) {
    val allProfiles: Flow<List<WireguardProfile>> = vpnDao.getAllProfiles()

    suspend fun insertProfile(profile: WireguardProfile) {
        vpnDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: WireguardProfile) {
        vpnDao.updateProfile(profile)
    }

    suspend fun deleteProfile(id: Int) {
        vpnDao.deleteProfile(id)
    }

    suspend fun connectToProfile(id: Int) {
        vpnDao.updateActiveProfile(id)
    }

    suspend fun disconnectAll() {
        vpnDao.disconnectAll()
    }
}
