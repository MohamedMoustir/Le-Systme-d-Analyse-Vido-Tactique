package com.savt.backend.application.service;

import com.mongodb.internal.bulk.UpdateRequest;
import com.savt.backend.application.mapper.UserMapper;
import com.savt.backend.domain.entity.User;
import com.savt.backend.domain.enums.Role;
import com.savt.backend.domain.exception.ResourceNotFoundException;
import com.savt.backend.domain.repository.UserRepository;
import com.savt.backend.presentation.dto.Request.UserUpdateDTO;
import com.savt.backend.presentation.dto.Response.UserResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServise {

    private  final UserRepository  userRepository ;
    private  final UserMapper  userMapper ;

    public List<UserResponseDTO> getAllUsers(){
      List<User> users =   userRepository.findAll();

        List<UserResponseDTO> responses =  users.stream()
              .filter(p->p.getRole().equals(Role.COACH))
              .map(p->userMapper.toResponse(p))
              .collect(Collectors.toList());

        return responses;
    }

    public UserResponseDTO updateUsers(UserUpdateDTO userUpdateDTO ,String id){
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        userMapper.updateUserFromDto(userUpdateDTO, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);

    }

    public boolean deleteUser(String id){
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        userRepository.delete(user);
        return true;

    }
}
