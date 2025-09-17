package com.elearning.service.services;

import com.elearning.service.dtos.AuthResponseDTO;
import com.elearning.service.dtos.LoginDTO;
import com.elearning.service.dtos.RegisterDTO;
import com.elearning.service.entities.User;
import com.elearning.service.repositories.UserRepository;
import com.elearning.service.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public String registerUser(RegisterDTO registerDto) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(registerDto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already registered!");
        }

        // Create new user
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setFullName(registerDto.getFullName());

        userRepository.save(user);
        return "User registered successfully!";
    }

    public AuthResponseDTO authenticateUser(LoginDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        String jwt = jwtTokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponseDTO(jwt, user.getId(), user.getEmail(), user.getFullName());
    }
}
