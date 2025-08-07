package com.krishna.letsshare

import com.fasterxml.jackson.databind.util.JSONPObject
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
            println("Device connected: $deviceId")
        } else {
            session.close()
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload

        val json = JSONObject(payload)
        val type = json.getString("type")

        when (type) {
            "init" -> {
                val sender = json.getString("senderId")
                val viewer = json.getString("viewerId")
                senderToViewer[sender] = viewer
                println("Viewer $viewer registered for sender $sender")
            }

            "image" -> {
                val sender = json.getString("senderId")
                val imageData = json.getString("data")

                val viewerId = senderToViewer[sender]
                val viewerSession = sessions[viewerId]

                if (viewerSession?.isOpen == true) {
                    viewerSession.sendMessage(TextMessage(payload))
                }
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val entry = sessions.entries.find { it.value == session }
        if (entry != null) {
            val deviceId = entry.key
            sessions.remove(deviceId)
            senderToViewer.entries.removeIf { it.key == deviceId || it.value == deviceId }
            println("Device disconnected: $deviceId")
        }
    }
}
