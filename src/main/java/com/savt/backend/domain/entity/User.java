package com.savt.backend.domain.entity;

import com.savt.backend.domain.enums.Role;
import com.savt.backend.domain.enums.Social;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection="users")
@Data
@Builder
public class User {
    @Id
    private String id;

    private String nom;
    private String email;
    private String password;
    private Role role;
    @CreatedDate
    private LocalDateTime creationAt;

    @LastModifiedDate
    private LocalDateTime updateAt;

    private Social provider;

    private boolean isActivated;

}
