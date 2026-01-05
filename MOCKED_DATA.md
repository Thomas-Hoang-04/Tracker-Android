# Mocked Data & Services

This document describes the mocked components in the application and verifies that they are used for **development and testing purposes** while the real backend is being developed.

---

## Overview

| Component | File | Purpose |
|-----------|------|---------|
| **Authentication** | `MockAuthRepository.kt` | Simulates login, registration, logout, password reset |
| **User Storage** | `MockUserStorage.kt` | In-memory user database with predefined accounts |
| **User Operations** | `MockUserRepository.kt` | Simulates profile retrieval and updates |
| **Order Data** | `MockOrderRepository.kt` | Provides sample orders for UI development |
| **BLE Device** | `MockBleManager.kt` | Simulates Bluetooth device scanning, connection, and communication |

---

## Mock Accounts

| Email | Password | Role |
|-------|----------|------|
| `test@example.com` | `password123` | CUSTOMER |
| `admin@example.com` | `admin123` | ADMIN |
| `provider@example.com` | `provider123` | PROVIDER |
| `shipper@example.com` | `shipper123` | SHIPPER |

---

## Detailed Descriptions

### 1. MockAuthRepository
**Why mocked?** Enables authentication flows without a live backend.

**What it does:**
- `register()` – Creates a new user in memory
- `login()` – Validates credentials against `MockUserStorage`
- `logout()` – Clears session tokens
- `forgotPassword()` / `resetPassword()` – Simulates password reset flow

---

### 2. MockUserStorage
**Why mocked?** Provides an in-memory user database.

**What it does:**
- Stores predefined mock users (CUSTOMER, ADMIN, PROVIDER, SHIPPER)
- Tracks currently logged-in user
- Supports adding new users during registration

---

### 3. MockUserRepository
**Why mocked?** Enables profile management without a backend.

**What it does:**
- `getCurrentUser()` – Returns the logged-in user's data
- `updateProfile()` – Updates user profile fields
- `changePassword()` – Validates and updates password

---

### 4. MockOrderRepository
**Why mocked?** Provides sample order data for UI development.

**What it does:**
- Stores a list of sample `OrderSummary` objects
- Supports adding new orders
- Used by Provider, Customer, and Shipper screens

---

### 5. MockBleManager
**Why mocked?** Simulates BLE device interactions without physical hardware.

**What it does:**
- `startScan()` – Simulates discovering 3 mock ESP32 devices
- `connect()` – Simulates connection handshake
- `sendToken()` / `sendThresholds()` – Simulates BLE write operations
- `deviceRequest` / `deviceResponse` – Emits simulated BLE protocol messages

---

## Notes

- All mocks use `delay()` to simulate realistic network/BLE latency.
- Tokens are stored in `TokenManager` using DataStore (persisted locally).
- When the real backend is ready, replace the mock repositories with real implementations via Hilt DI modules.
