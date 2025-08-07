package com.krishna.letsshare.model

data class DeviceRegisterRequest(
    val deviceId: String,
    val deviceName: String
)

data class DeviceRegisterResponse(
    val message: String
)