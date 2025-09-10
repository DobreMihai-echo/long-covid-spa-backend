package com.longcovidspa.backend.utils;

import com.longcovidspa.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtService;              // your existing JWT util
    private final UserDetailsServiceImpl userService;   // loads UserDetails by username

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc != null && StompCommand.CONNECT.equals(acc.getCommand())) {
            List<String> auth = acc.getNativeHeader("Authorization");
            String bearer = (auth != null && !auth.isEmpty()) ? auth.get(0) : null;
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);
                String username = jwtService.getUserNameFromToken(token);   // parse + validate
                if (username != null) {
                    UserDetails ud = userService.loadUserByUsername(username);
                    var authToken = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                    acc.setUser(authToken); // 👈 sets Principal for this WS session
                }
            }
        }
        return message;
    }
}

