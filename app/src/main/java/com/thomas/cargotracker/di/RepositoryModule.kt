package com.thomas.cargotracker.di

import com.google.gson.Gson
import com.thomas.cargotracker.keystore.TokenManager
import com.thomas.cargotracker.network.ApiInterface
import com.thomas.cargotracker.network.AuthInterface
import com.thomas.cargotracker.repository.AuthRepository
import com.thomas.cargotracker.repository.OrderRepository
import com.thomas.cargotracker.repository.OrderRepositoryImpl
import com.thomas.cargotracker.repository.ShipmentRepository
import com.thomas.cargotracker.repository.ShipmentRepositoryImpl
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
    fun provideUserRepository(
        apiInterface: ApiInterface,
        gson: Gson
    ): UserRepository = UserRepository(apiInterface, gson)

    @Provides
    @Singleton
    fun provideShipmentRepository(
        apiInterface: ApiInterface
    ): ShipmentRepository = ShipmentRepositoryImpl(apiInterface)

    @Provides
    @Singleton
    fun provideOrderRepository(
        apiInterface: ApiInterface
    ): OrderRepository = OrderRepositoryImpl(apiInterface)
}

