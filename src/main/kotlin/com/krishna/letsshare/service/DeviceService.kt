package com.krishna.letsshare.service

import com.krishna.letsshare.handler.ScreenShareHandler
import com.krishna.letsshare.model.DeviceRegisterRequest
import com.krishna.letsshare.model.DeviceRegisterResponse
import org.springframework.stereotype.Service


@Service
class DeviceService(
    private val screenShareHandler: ScreenShareHandler
){
    private val deviceRegisterRequests = mutableListOf<DeviceRegisterRequest>()

    fun registerDevice(deviceRegisterRequest: DeviceRegisterRequest): DeviceRegisterResponse {
        // avoid duplicates based on deviceId
        if (deviceRegisterRequests.none { it.deviceId == deviceRegisterRequest.deviceId }) {
            deviceRegisterRequests.add(deviceRegisterRequest)
        }
        return DeviceRegisterResponse("Device ${deviceRegisterRequest.deviceName} registered successfully.")
    }

    fun getAllDevices(): List<DeviceRegisterRequest> = deviceRegisterRequests.map {
        DeviceRegisterRequest(
            it.deviceId,
            it.deviceName,
            isStreaming = screenShareHandler.isDeviceStreaming(it.deviceId) ,
            isOnline = screenShareHandler.isDeviceOnline(it.deviceId)
        )
    }
}