package com.savt.backend.infrastructure.config;

import com.savt.backend.domain.entity.User;
import com.savt.backend.domain.enums.Role;
import com.savt.backend.domain.repository.UserRepository;

import com.savt.backend.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final SpringDataUserRepository userRepository ;

    @Override
    public void run(String... args) throws Exception {
    if(userRepository.count() == 0){
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw("Admin1234", salt);
        User users = User.builder()
               .nom("ADMIN")
                .email("admin@savatVideo.com")
                .password(hashedPassword)
                .isActivated(true)
                .role(Role.ADMIN)
                .build();
        userRepository.save(users);
        logger.info("Admin user created");

    }

    }
}
