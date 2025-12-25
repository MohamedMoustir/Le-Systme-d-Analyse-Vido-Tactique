package com.savt.backend.presentation.controller;

import com.savt.backend.application.service.UserServise;
import com.savt.backend.presentation.dto.Request.UserUpdateDTO;
import com.savt.backend.presentation.dto.Response.UserResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserServise userServise;

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
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO userUpdateDTO ,@PathVariable String id){
        UserResponseDTO users = userServise.updateUsers(userUpdateDTO ,id);
        return ResponseEntity.ok(users);
    }

}

