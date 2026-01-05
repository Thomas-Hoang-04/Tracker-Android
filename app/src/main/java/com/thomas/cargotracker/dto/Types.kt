package com.thomas.cargotracker.dto

import com.google.gson.annotations.SerializedName

enum class DeviceStatus {
    @SerializedName("AVAILABLE") AVAILABLE,
    @SerializedName("IN_TRANSIT") IN_TRANSIT,
    @SerializedName("MAINTENANCE") MAINTENANCE,
    @SerializedName("RETIRED") RETIRED
}

enum class ShipmentStatus {
    @SerializedName("PENDING") PENDING,
    @SerializedName("ASSIGNED") ASSIGNED,
    @SerializedName("IN_TRANSIT") IN_TRANSIT,
    @SerializedName("COMPLETED") COMPLETED,
    @SerializedName("CANCELLED") CANCELLED
}

enum class UserRole {
    @SerializedName("ADMIN") ADMIN,
    @SerializedName("CUSTOMER") CUSTOMER,
    @SerializedName("PROVIDER") PROVIDER,
    @SerializedName("SHIPPER") SHIPPER
}