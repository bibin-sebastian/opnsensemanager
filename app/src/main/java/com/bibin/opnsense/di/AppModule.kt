package com.bibin.opnsense.di

import android.content.Context
import androidx.room.Room
import com.bibin.opnsense.data.local.AppDatabase
import com.bibin.opnsense.data.local.DeviceAliasDao
import com.bibin.opnsense.data.remote.OPNsenseApi
import com.bibin.opnsense.data.remote.OPNsenseClient
import com.bibin.opnsense.util.CredentialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "opnsense.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_1_3)
            .build()

    @Provides
    fun provideDeviceAliasDao(db: AppDatabase): DeviceAliasDao = db.deviceAliasDao()

    @Provides
    @Singleton
    fun provideOPNsenseApi(credentialManager: CredentialManager): OPNsenseApi =
        OPNsenseClient.build(
            baseUrl = credentialManager.firewallUrl.ifBlank { "https://192.168.1.1" },
            apiKey = credentialManager.apiKey,
            apiSecret = credentialManager.apiSecret,
        )
}
