package com.football.analyzer.presentation.controller;


import com.football.analyzer.application.service.UserService;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.presentation.dto.Request.UserUpdateDTO;
import com.football.analyzer.presentation.dto.Response.UserResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userServise;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(){
        List<UserResponseDTO> users = userServise.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id){
        userServise.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("Client supprimé avec succès");
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'COACH')")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO userUpdateDTO , @PathVariable String id){
        UserResponseDTO users = userServise.updateUsers(userUpdateDTO ,id);
        return ResponseEntity.ok(users);
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String id) {
        userServise.toggleUserActivation(id);
        return ResponseEntity.ok().body(Map.of("message", "Statut de l'utilisateur mis à jour"));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(@PathVariable String id, @RequestParam String newRole) {
        userServise.changeUserRole(id, newRole);
        return ResponseEntity.ok().body("Rôle mis à jour avec succès");
    }


}

