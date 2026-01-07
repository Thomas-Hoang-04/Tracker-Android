package com.thomas.cargotracker.network

import com.thomas.cargotracker.dto.*
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

    // Order Endpoints (/api/orders)

    @POST("api/orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>

    @GET("api/orders")
    suspend fun getAllOrders(): Response<List<OrderResponse>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): Response<OrderResponse>

    @GET("api/orders/status/{status}")
    suspend fun getOrdersByStatus(
        @Path("status") status: OrderStatus
    ): Response<List<OrderResponse>>

    @GET("api/orders/pending")
    suspend fun getPendingOrders(): Response<List<OrderResponse>>

    @POST("api/orders/{id}/accept")
    suspend fun acceptOrder(
        @Path("id") id: String,
        @Body request: AcceptOrderRequest
    ): Response<OrderResponse>

    @POST("api/orders/{id}/reject")
    suspend fun rejectOrder(
        @Path("id") id: String,
        @Body request: RejectOrderRequest
    ): Response<OrderResponse>

    @POST("api/orders/filter")
    suspend fun filterOrders(
        @Body request: OrderFilterRequest
    ): Response<OrderListResponse>
}