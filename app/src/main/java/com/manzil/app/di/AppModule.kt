package com.manzil.app.di

import com.manzil.app.core.crypto.KeystoreSecretVault
import com.manzil.app.core.crypto.SecretVault
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSecretVault(impl: KeystoreSecretVault): SecretVault
}
