package com.krishna.letsshare.config

import com.krishna.letsshare.handler.ScreenShareHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean
import org.springframework.web.socket.server.support.DefaultHandshakeHandler

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(ScreenShareHandler(), "/ws/screen")
            .setAllowedOrigins("*")
            .setHandshakeHandler(DefaultHandshakeHandler())
    }

    @Bean
    fun createWebSocketContainer(): ServletServerContainerFactoryBean {
        val container = ServletServerContainerFactoryBean()
        container.setMaxTextMessageBufferSize(512 * 1024)  // 512 KB
        container.setMaxBinaryMessageBufferSize(512 * 1024) // 512 KB
        container.setAsyncSendTimeout(10_000L) // 10 seconds
        return container
    }
}