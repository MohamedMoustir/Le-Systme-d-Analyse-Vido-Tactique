package com.savt.backend.application.service;

import com.savt.backend.domain.entity.User;
import com.savt.backend.presentation.dto.Request.UserUpdateDTO;
import com.savt.backend.presentation.dto.Response.UserResponseDTO;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;


public interface  UserServise {
    public List<UserResponseDTO> getAllUsers();
    public UserResponseDTO updateUsers(UserUpdateDTO userUpdateDTO ,String id);
    public void deleteUser(String id);
    public User handleSocialLogin(OAuth2User principal, String registrationId);
}
