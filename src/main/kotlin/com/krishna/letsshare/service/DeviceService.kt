package com.krishna.letsshare.service

import com.krishna.letsshare.model.DeviceRegisterRequest
import com.krishna.letsshare.model.DeviceRegisterResponse
import org.springframework.stereotype.Service


@Service
class DeviceService {
    private val deviceRegisterRequests = mutableListOf<DeviceRegisterRequest>()

    fun registerDevice(deviceRegisterRequest: DeviceRegisterRequest): DeviceRegisterResponse {
        deviceRegisterRequests.add(deviceRegisterRequest)
        return DeviceRegisterResponse("Device ${deviceRegisterRequest.deviceName} registered successfully.")
    }

    fun getAllDevices(): List<DeviceRegisterRequest> = deviceRegisterRequests
}