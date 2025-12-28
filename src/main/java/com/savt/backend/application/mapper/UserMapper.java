package com.savt.backend.application.mapper;


import com.savt.backend.domain.entity.User;
import com.savt.backend.presentation.dto.Request.UserRequestDTO;
import com.savt.backend.presentation.dto.Request.UserUpdateDTO;
import com.savt.backend.presentation.dto.Response.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.*;
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponseDTO toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActivated", constant = "true")
    @Mapping(target = "creationAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "provider", ignore = true)
    User toEntity(UserRequestDTO request);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "creationAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "activated", ignore = true)
    @Mapping(target = "provider", ignore = true)
    void updateUserFromDto(UserUpdateDTO dto, @MappingTarget User entity);

}
