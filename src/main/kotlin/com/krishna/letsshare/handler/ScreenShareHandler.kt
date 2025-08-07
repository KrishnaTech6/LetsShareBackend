package com.krishna.letsshare.handler

import org.json.JSONObject
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class ScreenShareHandler : TextWebSocketHandler() {

    private val sessions = mutableMapOf<String, WebSocketSession>()  // deviceId -> session
    private val senderToViewer = mutableMapOf<String, String>()      // senderId -> viewerId

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

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = message.payload
            val json = JSONObject(payload)
            val type = json.getString("type")

            when (type) {
                "init" -> handleInit(json)
                "image" -> handleImage(json)
                else -> println("⚠️ Unknown message type: $type")
            }
        } catch (e: Exception) {
            println("⚠️ Error handling message: ${e.message}")
        }
    }

    private fun handleInit(json: JSONObject) {
        val senderId = json.getString("senderId")
        val viewerId = json.getString("viewerId")
        senderToViewer[senderId] = viewerId
        println("🔄 Routing image from sender [$senderId] to viewer [$viewerId]")
    }

    private fun handleImage(json: JSONObject) {
        val senderId = json.getString("senderId")
        val imageData = json.getString("data")

        val viewerId = senderToViewer[senderId]
        val viewerSession = viewerId?.let { sessions[it] }

        if (viewerSession?.isOpen == true) {
            viewerSession.sendMessage(TextMessage(json.toString()))
            println("📤 Frame sent from $senderId ➡️ $viewerId")
        } else {
            println("⚠️ Viewer session not available or closed for $viewerId")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val deviceId = sessions.entries.find { it.value == session }?.key
        if (deviceId != null) {
            sessions.remove(deviceId)
            senderToViewer.entries.removeIf { it.key == deviceId || it.value == deviceId }
            println("❌ Device disconnected: $deviceId")
        }
    }
}
