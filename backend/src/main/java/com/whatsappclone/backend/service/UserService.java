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

    public User register(String phone, String name, String password) {
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Telefone já cadastrado!");
        }

        User user = new User();
        user.setPhone(phone);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setOnline(false);

        return userRepository.save(user);
    }

    public Optional<String> login(String phone, String password) {
        return userRepository.findByPhone(phone)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    u.setOnline(true);
                    userRepository.save(u);
                    return jwtService.generateToken(u.getId(), u.getPhone());
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