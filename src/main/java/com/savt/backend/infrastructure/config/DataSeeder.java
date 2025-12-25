package com.smartshop.presontation.config;

import com.smartshop.application.service.ProductServise;
import com.smartshop.domain.enums.UserRole;
import com.smartshop.domain.model.Admin;
import com.smartshop.infrastructure.Repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminRepository adminRepository ;
    private final ProductServise productServise ;

    @Override
    public void run(String... args) throws Exception {
    if(adminRepository.count() == 0){
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw("Admin1234", salt);
        Admin admin = Admin.builder()
               .nom("Admin")
                .email("admin@smartshop.com")
                .password(hashedPassword)
                .role(UserRole.ADMIN)
                .build();
        adminRepository.save(admin);
        logger.info("Admin user created");

    }

    }
}
