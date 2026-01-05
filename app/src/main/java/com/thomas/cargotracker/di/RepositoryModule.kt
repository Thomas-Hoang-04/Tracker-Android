package com.thomas.cargotracker.di

import com.google.gson.Gson
import com.thomas.cargotracker.keystore.TokenManager
import com.thomas.cargotracker.network.ApiInterface
import com.thomas.cargotracker.network.AuthInterface
import com.thomas.cargotracker.repository.AuthRepository
import com.thomas.cargotracker.repository.DeviceRepository
import com.thomas.cargotracker.repository.MockAuthRepository
import com.thomas.cargotracker.repository.MockUserRepository
import com.thomas.cargotracker.repository.MockUserStorage
import com.thomas.cargotracker.repository.ShipmentRepository
import com.thomas.cargotracker.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authInterface: AuthInterface,
        tokenManager: TokenManager,
        gson: Gson
    ): AuthRepository = AuthRepository(authInterface, tokenManager, gson)

    @Provides
    @Singleton
    fun provideMockUserStorage(): MockUserStorage = MockUserStorage()

    @Provides
    @Singleton
    fun provideMockAuthRepository(
        tokenManager: TokenManager,
        mockUserStorage: MockUserStorage
    ): MockAuthRepository = MockAuthRepository(tokenManager, mockUserStorage)

    @Provides
    @Singleton
    fun provideMockUserRepository(
        mockUserStorage: MockUserStorage,
        tokenManager: TokenManager
    ): MockUserRepository = MockUserRepository(mockUserStorage, tokenManager)

    @Provides
    @Singleton
    fun provideUserRepository(
        apiInterface: ApiInterface,
        gson: Gson
    ): UserRepository = UserRepository(apiInterface, gson)

    @Provides
    @Singleton
    fun provideDeviceRepository(
        apiInterface: ApiInterface,
        gson: Gson
    ): DeviceRepository = DeviceRepository(apiInterface, gson)

    @Provides
    @Singleton
    fun provideShipmentRepository(
        apiInterface: ApiInterface,
        gson: Gson
    ): ShipmentRepository = ShipmentRepository(apiInterface, gson)
}

