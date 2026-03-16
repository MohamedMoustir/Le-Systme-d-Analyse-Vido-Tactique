package com.football.analyzer.domain.entity;

import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.domain.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    private String nom;
    private String email;
    private String password;
    private Role role;

    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @CreatedDate
    private LocalDateTime creationAt;

    @LastModifiedDate
    private LocalDateTime updateAt;
    private boolean activated; 

}
