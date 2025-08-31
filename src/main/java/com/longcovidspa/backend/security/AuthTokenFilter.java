package com.longcovidspa.backend.security;
import com.longcovidspa.backend.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
public class AuthTokenFilter extends OncePerRequestFilter {


    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       if (!request.getRequestURI().contains("/auth")) {
           final String authorizationHeader = request.getHeader("Authorization");

           String jwtToken = null;
           String username = null;
           if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
               jwtToken = authorizationHeader.substring(7);

               try {
                   username = jwtUtil.getUserNameFromToken(jwtToken);
               } catch (IllegalArgumentException e) {
                   System.out.printf("Unable to get JWT token");
               } catch (ExpiredJwtException e) {
                   System.out.println("JWT Token is expired");
               }
           } else {
               System.out.println("JWT Token does not start with Bearer ");
           }

           if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
               UserDetails userDetails = userDetailsService.loadUserByUsername(username);


               if (jwtUtil.validateToken(jwtToken, userDetails)) {
                   UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                   usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
               }
           }
       }

        filterChain.doFilter(request, response);
    }
}
