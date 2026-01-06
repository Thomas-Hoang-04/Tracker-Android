package com.thomas.cargotracker.network

import com.thomas.cargotracker.dto.AdminCreateUserRequest
import com.thomas.cargotracker.dto.AssignDeviceRequest
import com.thomas.cargotracker.dto.AssignShipperRequest
import com.thomas.cargotracker.dto.CancelShipmentRequest
import com.thomas.cargotracker.dto.ChangePasswordRequest
import com.thomas.cargotracker.dto.CompleteShipmentRequest
import com.thomas.cargotracker.dto.CreateShipmentRequest
import com.thomas.cargotracker.dto.LocationResponse
import com.thomas.cargotracker.dto.ShipmentFilterRequest
import com.thomas.cargotracker.dto.ShipmentListResponse
import com.thomas.cargotracker.dto.ShipmentResponse
import com.thomas.cargotracker.dto.ShipmentStatus
import com.thomas.cargotracker.dto.SuccessResponse
import com.thomas.cargotracker.dto.TelemetryResponse
import com.thomas.cargotracker.dto.UpdateProfileRequest
import com.thomas.cargotracker.dto.UpdateShipmentRequest
import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.dto.UserRole
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    // Admin User Management

    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("api/users/role/{role}")
    suspend fun getUsersByRole(
        @Path("role") role: UserRole
    ): Response<List<UserResponse>>

    @POST("api/auth/admin/users")
    suspend fun createUserByAdmin(
        @Body request: AdminCreateUserRequest
    ): Response<UserResponse>

    @POST("api/users/{id}/activate")
    suspend fun activateUser(
        @Path("id") id: String
    ): Response<SuccessResponse>

    @POST("api/users/{id}/deactivate")
    suspend fun deactivateUser(
        @Path("id") id: String
    ): Response<SuccessResponse>

    @GET("api/devices/{id}/telemetry")
    suspend fun getDeviceTelemetry(
        @Path("id") id: String
    ): Response<TelemetryResponse>

    @GET("api/devices/{id}/location")
    suspend fun getDeviceLocation(
        @Path("id") id: String
    ): Response<LocationResponse>

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