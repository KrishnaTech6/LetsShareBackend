package com.krishna.letsshare.handler

import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

class ScreenShareHandler : AbstractWebSocketHandler() {

    // Store active sessions mapped by deviceId
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val deviceId = session.uri?.query?.split("=")?.getOrNull(1)
        if (deviceId != null) {
            sessions[deviceId] = session
            println("🔌 Device connected: $deviceId")
        } else {
            println("❌ Connection rejected: No deviceId in query")
            session.close(CloseStatus.BAD_DATA)
        }
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val senderId = getSenderIdFromSession(session)
        val imageBytes = message.payload.array()
        println("📸 Received binary image from: $senderId (${imageBytes.size} bytes)")

        // Example: Broadcast to all other connected sessions (or selectively)
        sessions.forEach { (deviceId, otherSession) ->
            if (otherSession != session && otherSession.isOpen) {
                try {
                    otherSession.sendMessage(BinaryMessage(imageBytes))
                } catch (e: Exception) {
                    println("⚠️ Failed to forward to $deviceId: ${e.message}")
                }
            }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("⚠️ Received unexpected text message: ${message.payload}")
        // You can still support control messages here if needed
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val deviceId = sessions.entries.find { it.value == session }?.key
        if (deviceId != null) {
            sessions.remove(deviceId)
            println("❌ Device disconnected: $deviceId")
        }
    }

    private fun getSenderIdFromSession(session: WebSocketSession): String {
        return session.uri?.query?.split("=")?.getOrNull(1) ?: "unknown"
    }

    fun getDeviceSession(deviceId: String): WebSocketSession? = sessions[deviceId]
}
