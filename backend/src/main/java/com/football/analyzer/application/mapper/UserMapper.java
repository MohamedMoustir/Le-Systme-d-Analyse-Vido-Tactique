package com.football.analyzer.application.mapper;


import com.football.analyzer.domain.entity.User;
import com.football.analyzer.presentation.dto.Request.UserRequestDTO;
import com.football.analyzer.presentation.dto.Request.UserUpdateDTO;
import com.football.analyzer.presentation.dto.Response.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponseDTO toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActivated", constant = "true")
    @Mapping(target = "creationAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    User toEntity(UserRequestDTO request);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "creationAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "activated", ignore = true)
    void updateUserFromDto(UserUpdateDTO dto, @MappingTarget User entity);


}
