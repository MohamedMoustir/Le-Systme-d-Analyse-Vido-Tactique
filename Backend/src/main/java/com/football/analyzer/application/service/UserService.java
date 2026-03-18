package com.football.analyzer.application.service;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.presentation.dto.Request.UserUpdateDTO;
import com.football.analyzer.presentation.dto.Response.UserResponseDTO;


import java.util.List;
import java.util.Optional;


public interface UserService {

     List<UserResponseDTO> getAllUsers();
     UserResponseDTO updateUsers(UserUpdateDTO userUpdateDTO , String id);
     void deleteUser(String id);
    void toggleUserActivation(String id);
    void changeUserRole(String id, String newRole);
    Optional<User> getUserByEmail(String email);
}
