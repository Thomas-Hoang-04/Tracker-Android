package com.thomas.cargotracker.repository

import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.dto.UserRole
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockUserStorage @Inject constructor() {
    private val mockUsers = mutableMapOf(
        "test@example.com" to MockUser(
            id = "mock-user-001",
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            fullName = "Test User",
            phoneNumber = "+1234567890",
            role = UserRole.CUSTOMER,
            address = "123 Test Street"
        ),
        "admin@example.com" to MockUser(
            id = "mock-user-002",
            username = "admin",
            email = "admin@example.com",
            password = "admin123",
            fullName = "Admin User",
            phoneNumber = "+0987654321",
            role = UserRole.ADMIN,
            address = "456 Admin Ave"
        ),
        "provider@example.com" to MockUser(
            id = "mock-user-003",
            username = "provider",
            email = "provider@example.com",
            password = "provider123",
            fullName = "Provider User",
            phoneNumber = "+1122334455",
            role = UserRole.PROVIDER,
            address = "789 Provider Blvd"
        ),
        "shipper@example.com" to MockUser(
            id = "mock-user-004",
            username = "shipper",
            email = "shipper@example.com",
            password = "shipper123",
            fullName = "Shipper User",
            phoneNumber = "+5566778899",
            role = UserRole.SHIPPER,
            address = "321 Shipper Lane"
        )
    )

    val registeredUsers = mockUsers.toMutableMap()
    var currentLoggedInEmail: String? = null

    fun getUserByEmail(email: String): MockUser? = registeredUsers[email]

    fun getUserById(id: String): MockUser? = registeredUsers.values.find { it.id == id }

    fun getCurrentUser(): MockUser? = currentLoggedInEmail?.let { registeredUsers[it] }

    fun addUser(user: MockUser) {
        registeredUsers[user.email] = user
    }

    fun updateUser(email: String, update: (MockUser) -> MockUser) {
        registeredUsers[email]?.let { registeredUsers[email] = update(it) }
    }

    data class MockUser(
        val id: String,
        val username: String,
        val email: String,
        val password: String,
        val fullName: String,
        val phoneNumber: String?,
        val role: UserRole,
        val address: String?
    ) {
        fun toUserResponse(): UserResponse = UserResponse(
            id = id,
            username = username,
            email = email,
            fullName = fullName,
            phoneNumber = phoneNumber,
            role = role,
            address = address,
            isActive = true,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString()
        )
    }
}
