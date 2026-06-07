package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.AuthRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.AuthResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.RegisterRequestDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Role;
import io.github.ralucap11.medicalimagingtystem.entity.User;
import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import io.github.ralucap11.medicalimagingtystem.service.CustomUserDetailsService;
import io.github.ralucap11.medicalimagingtystem.service.JwtService;
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
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PATIENT);

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(jwt));
    }
}