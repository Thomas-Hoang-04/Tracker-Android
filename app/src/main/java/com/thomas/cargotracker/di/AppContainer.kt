package com.thomas.cargotracker.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.thomas.cargotracker.keystore.TokenManager
import com.thomas.cargotracker.network.ApiInterface
import com.thomas.cargotracker.network.AuthInterceptor
import com.thomas.cargotracker.network.AuthInterface
import com.thomas.cargotracker.repository.AuthRepository
import com.thomas.cargotracker.repository.DeviceRepository
import com.thomas.cargotracker.repository.ShipmentRepository
import com.thomas.cargotracker.repository.UserRepository
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel
import com.thomas.cargotracker.ui.viewmodel.DeviceViewModel
import com.thomas.cargotracker.ui.viewmodel.ShipmentViewModel
import com.thomas.cargotracker.ui.viewmodel.UserViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8081/"
    }

    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenManager)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiInterface: ApiInterface by lazy {
        retrofit.create(ApiInterface::class.java)
    }

    val authInterface: AuthInterface by lazy {
        retrofit.create(AuthInterface::class.java)
    }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(authInterface, tokenManager, gson)
    }

    val deviceRepository: DeviceRepository by lazy {
        DeviceRepository(apiInterface, gson)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(apiInterface, gson)
    }

    val shipmentRepository: ShipmentRepository by lazy {
        ShipmentRepository(apiInterface, gson)
    }

    // ViewModel Factories
    val authViewModelFactory: AuthViewModel.Factory by lazy {
        AuthViewModel.Factory(authRepository, userRepository)
    }

    val userViewModelFactory: UserViewModel.Factory by lazy {
        UserViewModel.Factory(userRepository)
    }

    val deviceViewModelFactory: DeviceViewModel.Factory by lazy {
        DeviceViewModel.Factory(deviceRepository)
    }

    val shipmentViewModelFactory: ShipmentViewModel.Factory by lazy {
        ShipmentViewModel.Factory(shipmentRepository)
    }
}
