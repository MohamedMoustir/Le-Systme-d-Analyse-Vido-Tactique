package com.football.analyzer.infrastructure.config;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.domain.repository.UserRepository;
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

    private final UserRepository userRepository ;

    @Override
    public void run(String... args) throws Exception {
    if(userRepository.count() == 0){
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw("Admin1234", salt);
        User users = User.builder()
                .nom("ADMIN")
                .email("admin@savatVideo.com")
                .password(hashedPassword)
                .activated(true)
                .role(Role.ADMIN)
                .build();
        userRepository.save(users);
        logger.info("Admin user created");

    }

    }
}
