package com.krishna.letsshare.model

data class DeviceRegisterRequest(
    val deviceId: String,
    val deviceName: String,
    val isOnline: Boolean,
    val isStreaming: Boolean
)

data class DeviceRegisterResponse(
    val message: String
)

data class DeviceInfoResponse(
    val deviceId: String,
    val deviceName: String,
    val isOnline: Boolean,
    val isStreaming: Boolean
)
