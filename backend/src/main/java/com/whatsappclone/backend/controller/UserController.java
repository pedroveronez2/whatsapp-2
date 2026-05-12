package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.service.AuditLogService;
import com.whatsappclone.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditLogService auditLogService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            User user = userService.register(username, password);
            auditLogService.log("REGISTER", username, request.getRemoteAddr(), "Usuário criado com sucesso");
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "message", "Usuário criado com sucesso!"
            ));
        } catch (RuntimeException e) {
            auditLogService.log("REGISTER_FAILED", username, request.getRemoteAddr(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<String> token = userService.login(username, password);

        if (token.isPresent()) {
            auditLogService.log("LOGIN", username, request.getRemoteAddr(), "Login realizado com sucesso");
            return ResponseEntity.ok(Map.of(
                "token", token.get(),
                "message", "Login realizado com sucesso!"
            ));
        } else {
            auditLogService.log("LOGIN_FAILED", username, request.getRemoteAddr(), "Senha inválida");
            return ResponseEntity.status(401).body(Map.of("error", "Usuário ou senha inválidos!"));
        }
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<?> logout(
            @PathVariable Long id,
            HttpServletRequest request) {
        userService.logout(id);
        auditLogService.log("LOGOUT", id.toString(), request.getRemoteAddr(), "Logout realizado");
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