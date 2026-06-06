package com.whatsappclone.backend.controller;

import com.whatsappclone.backend.model.User;
import com.whatsappclone.backend.service.AuditLogService;
import com.whatsappclone.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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
        String phone = body.get("phone");
        String name = body.get("name");
        String password = body.get("password");

        try {
            User user = userService.register(phone, name, password);
            auditLogService.log("REGISTER", phone, request.getRemoteAddr(), "Usuário criado com sucesso");
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "phone", user.getPhone(),
                "name", user.getName(),
                "message", "Usuário criado com sucesso!"
            ));
        } catch (RuntimeException e) {
            auditLogService.log("REGISTER_FAILED", phone, request.getRemoteAddr(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String phone = body.get("phone");
        String password = body.get("password");

        Optional<String> token = userService.login(phone, password);

        if (token.isPresent()) {
            auditLogService.log("LOGIN", phone, request.getRemoteAddr(), "Login realizado com sucesso");
            return ResponseEntity.ok(Map.of(
                "token", token.get(),
                "message", "Login realizado com sucesso!"
            ));
        } else {
            auditLogService.log("LOGIN_FAILED", phone, request.getRemoteAddr(), "Senha inválida");
            return ResponseEntity.status(401).body(Map.of("error", "Telefone ou senha inválidos!"));
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
                    "phone", u.getPhone(),
                    "name", u.getName(),
                    "online", u.isOnline()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        Long userId = (Long) ((UsernamePasswordAuthenticationToken) authentication).getDetails();
        return userService.findById(userId)
            .map(u -> ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "phone", u.getPhone(),
                "online", u.isOnline()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}