package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.Request.UserUpdateDTO;
import com.football.analyzer.presentation.dto.Response.UserResponseDTO;


import java.util.List;


public interface UserService {

     List<UserResponseDTO> getAllUsers();
     UserResponseDTO updateUsers(UserUpdateDTO userUpdateDTO , String id);
     void deleteUser(String id);

}
