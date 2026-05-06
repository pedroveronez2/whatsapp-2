package com.whatsappclone.backend.service;

import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
        user.setPassword(password);
        user.setOnline(false);

        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));

        // atualiza status para online ao logar
        userOpt.ifPresent(u -> {
            u.setOnline(true);
            userRepository.save(u);
        });

        return userOpt;
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