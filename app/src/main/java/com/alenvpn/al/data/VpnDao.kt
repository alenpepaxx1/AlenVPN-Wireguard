package com.alenvpn.al.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnDao {
    @Query("SELECT * FROM wireguard_profiles ORDER BY id DESC")
    fun getAllProfiles(): Flow<List<WireguardProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: WireguardProfile)

    @Update
    suspend fun updateProfile(profile: WireguardProfile)

    @Query("DELETE FROM wireguard_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Int)

    @Query("UPDATE wireguard_profiles SET isActive = (id = :id)")
    suspend fun updateActiveProfile(id: Int)

    @Query("UPDATE wireguard_profiles SET isActive = 0")
    suspend fun disconnectAll()
}
