package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.AuthRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.AuthResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.RegisterRequestDTO;
import io.github.ralucap11.medicalimagingtystem.service.CustomUserDetailsService;
import io.github.ralucap11.medicalimagingtystem.service.JwtService;
import io.github.ralucap11.medicalimagingtystem.service.RegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final RegisterService registerService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder, RegisterService registerService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.registerService = registerService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(@RequestBody AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponseDTO(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        PatientResponseDTO registeredPatient = registerService.register(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(registeredPatient.getEmail());
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(jwt));
    }
}