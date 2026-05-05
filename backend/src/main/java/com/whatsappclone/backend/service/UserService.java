package com.whatsappclone.backend.service;

import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username já existe!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // v1 = sem criptografia (de propósito!)
        user.setOnline(false);

        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }
}
