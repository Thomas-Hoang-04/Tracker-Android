package com.thomas.cargotracker.repository

import retrofit2.Response
import com.thomas.cargotracker.network.Result
import com.google.gson.Gson
import com.thomas.cargotracker.dto.ErrorResponse

abstract class BaseRepository(
    private val gson: Gson
) {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Empty response body", response.code())
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody) ?: "Unknown error occurred"
                Result.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error occurred")
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            errorBody?.let {
                gson.fromJson(it, ErrorResponse::class.java)?.message
            }
        } catch (e: Exception) {
            errorBody
        }
    }
}