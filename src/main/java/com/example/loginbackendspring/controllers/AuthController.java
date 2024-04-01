package com.example.loginbackendspring.controllers;

import com.example.loginbackendspring.domain.User;
import com.example.loginbackendspring.dto.LoginRequestDTO;
import com.example.loginbackendspring.dto.RegisterRequestDTO;
import com.example.loginbackendspring.dto.ResponseDTO;
import com.example.loginbackendspring.infra.security.TokenService;
import com.example.loginbackendspring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @RequestMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body){
        User user = this.userRepository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("User ("+body.email()+"/"+body.password()+") not found"));
        if (passwordEncoder.matches(body.password(),user.getPassword())) {
          String token = this.tokenService.generateToken(user);
          return ResponseEntity.ok( new ResponseDTO( user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body){
        // find User
        Optional<User> user = this.userRepository.findByEmail(body.email());
        if (user.isEmpty()) {
            // Create User
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            this.userRepository.save(newUser);
            // Generate Token for new User
            String token = this.tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

}
