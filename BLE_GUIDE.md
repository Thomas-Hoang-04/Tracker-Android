# Bluetooth Communication Protocol (Updated)

## Overview
Device **CHỦ ĐỘNG** thông báo cho Mobile App biết nó cần gì thông qua **Request Codes**.

### BLE Characteristics
- **Write (0x2A00)**: Mobile App → Device (gửi data)
- **Notify (0x2A01)**: Device → Mobile App (gửi requests & responses)

---

## Message Flow

### Scenario 1: First Time Setup (Device chưa có token)

```
Mobile App                    Device
    |                           |
    |------ Connect BLE ------->|
    |                           |
    |                           |--- Check NVS
    |                           |--- Token: NO
    |                           |--- Threshold: NO
    |                           |
    |<-- REQUEST TOKEN (code 1)-|  
    |   {                       |
    |     "requestCode": 1,     |
    |     "message": "Device    |
    |        needs token"       |
    |   }                       |
    |                           |
    | [User nhập token on app]  |
    |                           |
    |-- Send Token ------------>|
    |   { "token": "abc123" }   |
    |                           |--- Save to NVS
    |                           |--- Setup MQTT
    |                           |
    |<- ACK Token OK (code 10)--|
    |   {                       |
    |     "responseCode": 10,   |
    |     "message": "Token     |
    |       saved successfully" |
    |   }                       |
    |                           |
    |                           |--- Need threshold?
    |                           |
    |<-- REQUEST THRESHOLD -----|  (code 2)
    |   {                       |
    |     "requestCode": 2,     |
    |     "message": "Device    |
    |        needs threshold"   |
    |   }                       |
    |                           |
    | [User nhập thresholds]    |
    |                           |
    |-- Send Thresholds ------->|
    |   {                       |
    |     "temp_min": 20,       |
    |     "read_period_ms": 30000|
    |     ...                   |
    |   }                       |
    |                           |--- Save to NVS
    |                           |
    |<- ACK Threshold OK -------|  (code 20)
    |   {                       |
    |     "responseCode": 20,   |
    |     "message": "Thresholds|
    |       saved successfully" |
    |   }                       |
    |                           |
    |                           |--- Start sensors
    |                           |--- Disconnect BLE
```

### Scenario 2: Device đã có token, chưa có threshold

```
Mobile App                    Device
    |                           |
    |------ Connect BLE ------->|
    |                           |
    |                           |--- Check NVS
    |                           |--- Token: YES ✓
    |                           |--- Threshold: NO
    |                           |
    |<-- REQUEST THRESHOLD -----|  (code 2)
    |   {                       |
    |     "requestCode": 2,     |
    |     "message": "Device    |
    |        needs threshold"   |
    |   }                       |
    |                           |
    |-- Send Thresholds ------->|
    |                           |--- Save to NVS
    |                           |
    |<- ACK Threshold OK -------|  (code 20)
    |                           |
    |                           |--- Disconnect BLE
```

### Scenario 3: Device đã có đủ cả 2

```
Mobile App                    Device
    |                           |
    |------ Connect BLE ------->|
    |                           |
    |                           |--- Check NVS
    |                           |--- Token: YES ✓
    |                           |--- Threshold: YES ✓
    |                           |
    |<-- DEVICE READY (code 3)--|
    |   {                       |
    |     "requestCode": 3,     |
    |     "message": "Device    |
    |        is ready"          |
    |   }                       |
    |                           |
    | [App có thể reconfigure]  |
    |                           |
    |-- Send New Thresholds --->|  (optional)
    |                           |
    |<- ACK Threshold OK -------|
```

---

## Code Definitions

### Request Codes (Device → Mobile App)
Các mã này **DEVICE CHỦ ĐỘNG GỬI** ngay sau khi kết nối BLE:

```cpp
DEVICE_REQUEST_TOKEN = 1       // "Tôi cần token"
DEVICE_REQUEST_THRESHOLD = 2   // "Tôi cần threshold"
DEVICE_READY = 3               // "Tôi đã sẵn sàng"
```

### Response Codes (Device → Mobile App)
Các mã này device gửi **SAU KHI NHẬN** data từ mobile:

```cpp
RESPONSE_TOKEN_OK = 10         // Token saved OK
RESPONSE_TOKEN_ERROR = 11      // Token save failed
RESPONSE_THRESHOLD_OK = 20     // Threshold saved OK
RESPONSE_THRESHOLD_ERROR = 21  // Threshold save failed
```

---

## Message Formats

### 1. Device → Mobile App (Requests)

#### Request Token (code 1)
```json
{
  "requestCode": 1,
  "message": "Device needs token"
}
```

#### Request Threshold (code 2)
```json
{
  "requestCode": 2,
  "message": "Device needs threshold settings"
}
```

#### Device Ready (code 3)
```json
{
  "requestCode": 3,
  "message": "Device is ready"
}
```

### 2. Mobile App → Device (Data)

#### Token Data
```json
{
  "token": "device_token_abc123"
}
```

#### Threshold Data
```json
{
  "temp_min": 20.0,
  "temp_max": 30.0,
  "humidity_min": 30.0,
  "humidity_max": 60.0,
  "gas_threshold": 100,
  "accel_threshold": 1.0,
  "gyro_threshold": 1.0,
  "read_period_ms": 30000
}
```

### 3. Device → Mobile App (Responses)

#### Token Response
```json
// Success (code 10)
{
  "responseCode": 10,
  "message": "Token saved successfully",
  "timestamp": 1234567890
}

// Error (code 11)
{
  "responseCode": 11,
  "message": "Failed to save token to NVS",
  "timestamp": 1234567890
}
```

#### Threshold Response
```json
// Success (code 20)
{
  "responseCode": 20,
  "message": "Thresholds saved successfully",
  "timestamp": 1234567891
}

// Error (code 21)
{
  "responseCode": 21,
  "message": "Invalid threshold format",
  "timestamp": 1234567891
}
```

---

## Mobile App Implementation

### 1. Subscribe to Notifications
```kotlin
// Khi connect thành công
val notifyCharacteristic = service.getCharacteristic(UUID_NOTIFY)
bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, true)

val descriptor = notifyCharacteristic.getDescriptor(UUID_CCCD)
descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
bluetoothGatt.writeDescriptor(descriptor)
```

### 2. Handle Incoming Notifications
```kotlin
override fun onCharacteristicChanged(
    gatt: BluetoothGatt,
    characteristic: BluetoothGattCharacteristic
) {
    val response = characteristic.value.toString(Charsets.UTF_8)
    val json = JSONObject(response)
    
    // Check for Request Code (device cần gì)
    if (json.has("requestCode")) {
        val requestCode = json.getInt("requestCode")
        when(requestCode) {
            1 -> showTokenInputDialog()        // Device cần token
            2 -> showThresholdInputDialog()    // Device cần threshold
            3 -> showDeviceReady()             // Device sẵn sàng
        }
    }
    
    // Check for Response Code (kết quả xử lý)
    if (json.has("responseCode")) {
        val responseCode = json.getInt("responseCode")
        when(responseCode) {
            10 -> onTokenSavedSuccess()
            11 -> onTokenSavedError(json.getString("message"))
            20 -> onThresholdSavedSuccess()
            21 -> onThresholdSavedError(json.getString("message"))
        }
    }
}
```

### 3. State Machine
```kotlin
enum class BleSetupState {
    DISCONNECTED,
    CONNECTED,                  // BLE connected
    WAITING_FOR_REQUEST,        // Chờ device gửi request code
    TOKEN_REQUESTED,            // Device yêu cầu token (code 1)
    TOKEN_SENDING,              // Đang gửi token
    TOKEN_CONFIRMED,            // Nhận được ACK (code 10)
    THRESHOLD_REQUESTED,        // Device yêu cầu threshold (code 2)
    THRESHOLD_SENDING,          // Đang gửi threshold
    THRESHOLD_CONFIRMED,        // Nhận được ACK (code 20)
    DEVICE_READY,               // Device ready (code 3)
    SETUP_COMPLETE
}

var currentState = BleSetupState.DISCONNECTED

fun handleNotification(json: JSONObject) {
    when {
        json.has("requestCode") -> {
            val code = json.getInt("requestCode")
            when (code) {
                1 -> {
                    currentState = BleSetupState.TOKEN_REQUESTED
                    showTokenInput()
                }
                2 -> {
                    currentState = BleSetupState.THRESHOLD_REQUESTED
                    showThresholdInput()
                }
                3 -> {
                    currentState = BleSetupState.DEVICE_READY
                    showSuccessMessage()
                }
            }
        }
        json.has("responseCode") -> {
            val code = json.getInt("responseCode")
            when (code) {
                10 -> {
                    currentState = BleSetupState.TOKEN_CONFIRMED
                    // Device sẽ tự động request threshold nếu cần
                }
                11 -> {
                    showError(json.getString("message"))
                    currentState = BleSetupState.TOKEN_REQUESTED
                }
                20 -> {
                    currentState = BleSetupState.THRESHOLD_CONFIRMED
                    showSuccess("Configuration complete!")
                }
                21 -> {
                    showError(json.getString("message"))
                    currentState = BleSetupState.THRESHOLD_REQUESTED
                }
            }
        }
    }
}
```

### 4. Send Data with Timeout
```kotlin
fun sendToken(token: String) {
    currentState = BleSetupState.TOKEN_SENDING
    
    val json = JSONObject()
    json.put("token", token)
    
    val writeCharacteristic = service.getCharacteristic(UUID_WRITE)
    writeCharacteristic.value = json.toString().toByteArray()
    bluetoothGatt.writeCharacteristic(writeCharacteristic)
    
    // Setup timeout
    handler.postDelayed({
        if (currentState == BleSetupState.TOKEN_SENDING) {
            showError("Timeout: Device did not respond")
            currentState = BleSetupState.TOKEN_REQUESTED
        }
    }, 5000)
}

fun sendThresholds(thresholds: ThresholdSettings) {
    currentState = BleSetupState.THRESHOLD_SENDING
    
    val json = JSONObject()
    json.put("temp_min", thresholds.tempMin)
    json.put("temp_max", thresholds.tempMax)
    json.put("humidity_min", thresholds.humidityMin)
    json.put("humidity_max", thresholds.humidityMax)
    json.put("gas_threshold", thresholds.gasThreshold)
    json.put("accel_threshold", thresholds.accelThreshold)
    json.put("gyro_threshold", thresholds.gyroThreshold)
    json.put("read_period_ms", thresholds.readPeriodMs)
    
    val writeCharacteristic = service.getCharacteristic(UUID_WRITE)
    writeCharacteristic.value = json.toString().toByteArray()
    bluetoothGatt.writeCharacteristic(writeCharacteristic)
    
    // Setup timeout
    handler.postDelayed({
        if (currentState == BleSetupState.THRESHOLD_SENDING) {
            showError("Timeout: Device did not respond")
            currentState = BleSetupState.THRESHOLD_REQUESTED
        }
    }, 5000)
}
```

---

## Advantages of This Design

✅ **Device chủ động**: Device tự báo nó cần gì (token/threshold/ready)
✅ **Mobile app passive**: Mobile app chỉ phản ứng theo request từ device
✅ **Clear separation**: Request codes (1-3) vs Response codes (10, 11, 20, 21)
✅ **No ambiguity**: Mobile app biết chính xác device đang ở trạng thái nào
✅ **Error handling**: Device báo lỗi chi tiết nếu có vấn đề
✅ **Retry-able**: Mobile app có thể gửi lại nếu nhận error response
✅ **Stateful**: Dễ implement state machine cho complex flow

---

## Testing Checklist

### Device Side
- [ ] Sau khi BLE connect, gửi ngay request code phù hợp
- [ ] Request code 1 nếu chưa có token
- [ ] Request code 2 nếu có token nhưng chưa có threshold
- [ ] Request code 3 nếu đã có đủ cả 2
- [ ] Gửi response code 10/11 sau khi nhận token
- [ ] Gửi response code 20/21 sau khi nhận threshold
- [ ] Disconnect BLE sau khi hoàn tất

### Mobile App Side
- [ ] Subscribe notification ngay sau khi connect
- [ ] Nhận và parse request code (1, 2, 3)
- [ ] Hiển thị UI phù hợp theo request code
- [ ] Gửi data theo đúng format
- [ ] Nhận và parse response code (10, 11, 20, 21)
- [ ] Hiển thị success/error message
- [ ] Implement timeout mechanism
- [ ] Có thể retry nếu timeout/error