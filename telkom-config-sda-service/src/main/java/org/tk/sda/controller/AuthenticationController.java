package org.tk.sda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.tk.sda.config.util.JwtUtil;
import org.tk.sda.model.AuthRequest;
import org.tk.sda.model.AuthResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // Simple "authentication" - just check username/password equals some test values
            if ("testuser".equals(authRequest.getUsername()) && "password".equals(authRequest.getPassword())) {

                // Hardcode roles for testing
                List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

                String token = jwtUtil.generateToken(authRequest.getUsername(), roles);
                return ResponseEntity.ok(new AuthResponse(token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

}
