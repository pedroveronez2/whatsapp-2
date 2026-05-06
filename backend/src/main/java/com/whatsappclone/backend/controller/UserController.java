package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            User user = userService.register(username, password);
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "message", "Usuário criado com sucesso!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> user = userService.login(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "id", user.get().getId(),
                "username", user.get().getUsername(),
                "online", user.get().isOnline(),
                "message", "Login realizado com sucesso!"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Usuário ou senha inválidos!"));
        }
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<?> logout(@PathVariable Long id) {
        userService.logout(id);
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso!"));
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(Map.of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "online", u.isOnline()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}