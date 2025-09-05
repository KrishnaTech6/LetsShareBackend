package com.krishna.letsshare.handler

import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.util.concurrent.ConcurrentHashMap


@Component
class ScreenShareHandler : AbstractWebSocketHandler() {

    // deviceId -> sender session
    private val senders = ConcurrentHashMap<String, WebSocketSession>()

    // deviceId -> viewer session (single viewer)
    private val viewers = ConcurrentHashMap<String, WebSocketSession>()

    private val streamingStatus = ConcurrentHashMap<String, Boolean>()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val senderDeviceId = senders.entries.find { it.value == session }?.key
        if (senderDeviceId != null) {
            streamingStatus[senderDeviceId] = true
            viewers[senderDeviceId]?.let { viewerSession ->
                if (viewerSession.isOpen) {
                    try {
                        viewerSession.sendMessage(message)
                    } catch (e: Exception) {
                        println("Failed to send frame to viewer of $senderDeviceId: ${e.message}")
                    }
                }
            }
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val query = session.uri?.query ?: ""
        val role = getParam(query, "role")
        when (role) {
            "sender" -> {
                val deviceId = getParam(query, "deviceId")
                if (deviceId.isNullOrBlank()) {
                    session.close(CloseStatus.BAD_DATA)
                    println("Sender connection rejected (no deviceId)")
                } else {
                    if (senders.containsKey(deviceId)) {
                        // Device is already connected, reject new one
                        println("Sender connection ignored (already connected): $deviceId")
                        try { session.close(CloseStatus.NORMAL) } catch (_: Exception) {}
                        return
                    }
                    senders[deviceId]?.let { oldSession ->
                        try { oldSession.close(CloseStatus.NORMAL) } catch (_: Exception) {}
                    }
                    senders[deviceId] = session
                    streamingStatus[deviceId] = false
                    println("🔌 Sender connected: $deviceId")
                }
            }
            "viewer" -> {
                val watch = getParam(query, "watch")
                if (watch.isNullOrBlank()) {
                    session.close(CloseStatus.BAD_DATA)
                    println("Viewer connection rejected (no watch param)")
                } else {
                    // Remove this session from ANY other device viewer slot before assigning
                    viewers.entries.removeIf { (_, existingSession) ->
                        if (existingSession == session) {
                            println("Removing stale viewer session from another device")
                            true
                        } else false
                    }
                    // Close previous viewer for this device (if any)
                    viewers[watch]?.let { oldViewer ->
                        try { oldViewer.close(CloseStatus.NORMAL) } catch (_: Exception) {}
                    }

                    viewers[watch] = session
                    println("Viewer connected for device: $watch")
                }
            }
            else -> {
                session.close(CloseStatus.BAD_DATA)
                println("Connection rejected (missing/unknown role)")
            }
        }
    }

    // small text messages (optional controls)
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload.trim()
        println("Text message from ${session.id}: $payload")

        // If viewer sends a "start" command, forward it to the sender device
        if (payload.startsWith("START_STREAM:")) {
            val deviceId = payload.removePrefix("START_STREAM:").trim()
            senders[deviceId]?.let { senderSession ->
                if (senderSession.isOpen) {
                    senderSession.sendMessage(TextMessage("START_STREAM"))
                    println("Sent START_STREAM to $deviceId")
                }
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        // remove if present in senders
        val removedSender = senders.entries.find { it.value == session }?.key

        if (removedSender != null) {
            senders.remove(removedSender)
            println("Sender disconnected: $removedSender")
        }
        // remove if present in viewers
        val removedViewer = viewers.entries.find { it.value == session }?.key
        if (removedViewer != null) {
            viewers.remove(removedViewer)
            println("Viewer disconnected for device: $removedViewer")
        }
    }

    private fun getParam(query: String, key: String): String? {
        if (query.isBlank()) return null
        return query.split("&").mapNotNull {
            val parts = it.split("=")
            if (parts.size == 2 && parts[0] == key) parts[1] else null
        }.firstOrNull()
    }

    // optional: helper for other code to know if a device is online
    fun isDeviceOnline(deviceId: String): Boolean = senders.containsKey(deviceId)

    fun isDeviceStreaming(deviceId: String): Boolean = streamingStatus[deviceId] == true

    fun sendStartCommand(deviceId: String) {
        senders[deviceId]?.let {
            if (it.isOpen) {
                it.sendMessage(TextMessage("START_STREAM"))
            }
        }
    }

}
