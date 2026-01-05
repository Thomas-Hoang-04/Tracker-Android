package com.thomas.cargotracker.dto

data class SuccessResponse(
    val message: String,
    val data: Any? = null
)

data class ErrorResponse(
    val message: String,
    val error: String,
    val status: Int,
    val timestamp: String,
    val path: String? = null
)