package com.longcovidspa.backend.config;

import com.longcovidspa.backend.utils.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.ws.allowed-origins}")
    private String wsOrigins;
    private final StompAuthChannelInterceptor stompAuth;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] patterns = Arrays.stream(wsOrigins.split(",")).map(String::trim).toArray(String[]::new);
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(patterns)   // FRONTEND origins only
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry r) {
        r.enableSimpleBroker("/queue");
        r.setUserDestinationPrefix("/user");
        r.setApplicationDestinationPrefixes("/app");
    }
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuth);
    }
}
