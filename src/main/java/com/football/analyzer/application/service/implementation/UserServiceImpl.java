package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.mapper.UserMapper;
import com.football.analyzer.application.service.UserService;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.domain.exception.ResourceNotFoundException;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.presentation.dto.request.UserUpdateDTO;
import com.football.analyzer.presentation.dto.response.UserResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {


    private  final UserRepository userRepository ;
    private  final UserMapper userMapper ;

    public List<UserResponseDTO> getAllUsers(){
        List<User> users =   userRepository.findAll();

        return users.stream()
                .filter(p->p.getRole().equals(Role.COACH))
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUsers(UserUpdateDTO userUpdateDTO , String id){
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        userMapper.updateUserFromDto(userUpdateDTO, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);

    }

    public void deleteUser(String id){
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        userRepository.delete(user);

    }

    @Override
    public void toggleUserActivation(String id) {
        userRepository.findById(id)
                .map(user -> {
                    user.setActivated(!user.isActivated());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
    }

    @Override
    public void changeUserRole(String id, String newRole) {
        userRepository.findById(id)
                .map(user -> {
                    user.setRole(Role.valueOf(newRole));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
