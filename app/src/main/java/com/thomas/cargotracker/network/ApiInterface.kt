package com.thomas.cargotracker.network

import com.thomas.cargotracker.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    // User Endpoints (/api/users)

    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("api/users/me/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    @PUT("api/users/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<SuccessResponse>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): Response<UserResponse>

    // Device Endpoints (/api/devices)

    @POST("api/devices")
    suspend fun createDevice(
        @Body request: CreateDeviceRequest
    ): Response<DeviceResponse>

    @GET("api/devices/{id}")
    suspend fun getDeviceById(
        @Path("id") id: String
    ): Response<DeviceResponse>

    @GET("api/devices")
    suspend fun getAllDevices(): Response<List<DeviceResponse>>

    @GET("api/devices/status/{status}")
    suspend fun getDevicesByStatus(
        @Path("status") status: DeviceStatus
    ): Response<List<DeviceResponse>>

    @GET("api/devices/hardware/{hardwareUid}")
    suspend fun getDeviceByHardwareUid(
        @Path("hardwareUid") hardwareUid: String
    ): Response<DeviceResponse>

    @GET("api/devices/offline")
    suspend fun getOfflineDevices(
        @Query("thresholdMillis") thresholdMillis: Long = 300000
    ): Response<List<DeviceResponse>>

    @GET("api/devices/online")
    suspend fun getOnlineDevices(
        @Query("thresholdMillis") thresholdMillis: Long = 300000
    ): Response<List<DeviceResponse>>

    @GET("api/devices/shipment/{shipmentId}")
    suspend fun getDevicesByShipmentId(
        @Path("shipmentId") shipmentId: String
    ): Response<List<DeviceResponse>>

    @PUT("api/devices/{id}")
    suspend fun updateDevice(
        @Path("id") id: String,
        @Body request: UpdateDeviceRequest
    ): Response<DeviceResponse>

    @PATCH("api/devices/{id}/status")
    suspend fun updateDeviceStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequest
    ): Response<DeviceResponse>

    @DELETE("api/devices/{id}")
    suspend fun deleteDevice(
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/devices/{id}/assign/{shipmentId}")
    suspend fun assignDeviceToShipment(
        @Path("id") deviceId: String,
        @Path("shipmentId") shipmentId: String
    ): Response<DeviceResponse>

    @POST("api/devices/{id}/release")
    suspend fun releaseDeviceFromShipment(
        @Path("id") id: String
    ): Response<DeviceResponse>

    @POST("api/devices/filter")
    suspend fun filterDevices(
        @Body request: DeviceFilterRequest
    ): Response<DeviceListResponse>

    // Shipment Endpoints (/api/shipments)

    @POST("api/shipments")
    suspend fun createShipment(
        @Body request: CreateShipmentRequest
    ): Response<ShipmentResponse>

    @GET("api/shipments/{id}")
    suspend fun getShipmentById(
        @Path("id") id: String
    ): Response<ShipmentResponse>

    @GET("api/shipments")
    suspend fun getAllShipments(): Response<List<ShipmentResponse>>

    @GET("api/shipments/status/{status}")
    suspend fun getShipmentsByStatus(
        @Path("status") status: ShipmentStatus
    ): Response<List<ShipmentResponse>>

    @PUT("api/shipments/{id}")
    suspend fun updateShipment(
        @Path("id") id: String,
        @Body request: UpdateShipmentRequest
    ): Response<ShipmentResponse>

    @POST("api/shipments/{id}/assign-shipper")
    suspend fun assignShipper(
        @Path("id") id: String,
        @Body request: AssignShipperRequest
    ): Response<ShipmentResponse>

    @POST("api/shipments/{id}/assign-device")
    suspend fun assignDevice(
        @Path("id") id: String,
        @Body request: AssignDeviceRequest
    ): Response<ShipmentResponse>

    @POST("api/shipments/{id}/start-transit")
    suspend fun startTransit(
        @Path("id") id: String
    ): Response<ShipmentResponse>

    @POST("api/shipments/{id}/complete")
    suspend fun completeShipment(
        @Path("id") id: String,
        @Body request: CompleteShipmentRequest
    ): Response<ShipmentResponse>

    @POST("api/shipments/{id}/cancel")
    suspend fun cancelShipment(
        @Path("id") id: String,
        @Body request: CancelShipmentRequest
    ): Response<ShipmentResponse>

    @POST("api/shipments/filter")
    suspend fun filterShipments(
        @Body request: ShipmentFilterRequest
    ): Response<ShipmentListResponse>
}