package com.savt.backend.application.service.implementation;

import com.savt.backend.application.mapper.UserMapper;
import com.savt.backend.application.mapper.VideoMapper;
import com.savt.backend.application.service.AuthService;
import com.savt.backend.application.service.FileStorageService;
import com.savt.backend.application.service.UserServise;
import com.savt.backend.domain.entity.User;
import com.savt.backend.domain.enums.Role;
import com.savt.backend.domain.enums.Social;
import com.savt.backend.domain.exception.ResourceNotFoundException;
import com.savt.backend.domain.repository.UserRepository;
import com.savt.backend.domain.repository.VideoRepository;
import com.savt.backend.presentation.dto.Request.UserUpdateDTO;
import com.savt.backend.presentation.dto.Response.UserResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserServise {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> users =   userRepository.findAll();

        List<UserResponseDTO> responses =  users.stream()
                .filter(p->p.getRole().equals(Role.COACH))
                .map(p->userMapper.toResponse(p))
                .collect(Collectors.toList());

        return responses;
    }

    @Override
    public UserResponseDTO updateUsers(UserUpdateDTO userUpdateDTO, String id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        userMapper.updateUserFromDto(userUpdateDTO, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        userRepository.deleteById(id);

    }

    @Override
    public User handleSocialLogin(OAuth2User principal ,String registrationId) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            User newUser = User.builder()
                    .nom(name)
                    .email(email)
                    .isActivated(true)
                    .role(Role.COACH)
                    .provider(Social.valueOf(registrationId.toUpperCase()))
                    .creationAt(LocalDateTime.now())
                    .password(null)
                    .build();
            return  userRepository.save(newUser);

        }

    }

}
