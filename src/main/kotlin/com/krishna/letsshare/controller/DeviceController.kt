package com.krishna.letsshare.controller

import com.krishna.letsshare.model.DeviceRegisterRequest
import com.krishna.letsshare.model.DeviceRegisterResponse
import com.krishna.letsshare.service.DeviceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@CrossOrigin // Enable if testing from Android emulator to localhost
class DeviceController(
    private val deviceService: DeviceService
) {

    @PostMapping("/register")
    fun registerDevice(@RequestBody deviceRegisterRequest: DeviceRegisterRequest): ResponseEntity<DeviceRegisterResponse> {
        val result = deviceService.registerDevice(deviceRegisterRequest)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/devices")
    fun getDevices(): ResponseEntity<List<DeviceRegisterRequest>> {
        return ResponseEntity.ok(deviceService.getAllDevices())
    }
}