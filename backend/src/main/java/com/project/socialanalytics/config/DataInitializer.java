package com.project.socialanalytics.config;

import com.project.socialanalytics.entity.User;
import com.project.socialanalytics.repository.UserRepository;
import com.project.socialanalytics.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("analyst")) {
            User analyst = new User(
                    "analyst",
                    "analyst@sociallens.edu",
                    passwordEncoder.encode("analyst123"),
                    Role.ANALYST
            );
            userRepository.save(analyst);
            logger.info("Initialized default analyst user: analyst / analyst123");
        }

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    "admin@sociallens.edu",
                    passwordEncoder.encode("admin123"),
                    Role.ADMIN
            );
            userRepository.save(admin);
            logger.info("Initialized default admin user: admin / admin123");
        }
    }
}

