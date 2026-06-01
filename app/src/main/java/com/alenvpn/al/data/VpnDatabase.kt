// Copyright Alen Pepa

package com.alenvpn.al.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WireguardProfile::class], version = 3, exportSchema = false)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun vpnDao(): VpnDao

    companion object {
        @Volatile
        private var Instance: VpnDatabase? = null

        fun getDatabase(context: Context): VpnDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, VpnDatabase::class.java, "vpn_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
