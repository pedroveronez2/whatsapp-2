package com.whatsappclone.backend.service;

import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username já existe!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // bcrypt aqui!
        user.setOnline(false);

        return userRepository.save(user);
    }

    public Optional<String> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    u.setOnline(true);
                    userRepository.save(u);
                    return jwtService.generateToken(u.getId(), u.getUsername());
                });
    }

    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setOnline(false);
            userRepository.save(u);
        });
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}