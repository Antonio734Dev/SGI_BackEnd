package com.labMetricas.LabMetricas.config;

import com.labMetricas.LabMetricas.role.model.Role;
import com.labMetricas.LabMetricas.role.repository.RoleRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // Initialize roles
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("SUPERVISOR");
            createRoleIfNotFound("OPERADOR");

            // Create default users
            createDefaultUsers();

            logger.info("Database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during database initialization: " + e.getMessage(), e);
            throw e;
        }
    }


    private void createDefaultUsers() {
        // Administrador del Sistema
        createUserIfNotExists(
            "Antonio García González", 
            "antoniogarciagonzalez212@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Administrador del Sistema - Amador Casillas
        createUserIfNotExists(
            "Amador Casillas", 
            "amadorcasillasdr@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Supervisor
        createUserIfNotExists(
            "Supervisor UTEZ", 
            "20233tn106@utez.edu.mx", 
            "Super2024#Lab", 
            "SUPERVISOR",
            "Supervisor de Laboratorio"
        );

        // Operador
        createUserIfNotExists(
            "Desarrollador LabMétricas", 
            "labmetricasdev@gmail.com", 
            "Oper2024#Lab", 
            "OPERADOR",
            "Operador de Laboratorio"
        );
    }

    private void createUserIfNotExists(
            String name, 
            String email, 
            String password, 
            String roleName,
            String position
    ) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPosition(position);
            user.setRole(roleRepository.findByName(roleName).orElseThrow());
            user.setEnabled(true);
            user.setStatus(true);

            // Optional: Add phone number if needed
            // user.setPhone("5551234567");

            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Created user: {} ({}) with role {}", name, email, roleName);
        }
    }

    private void createRoleIfNotFound(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name);
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            roleRepository.save(role);
            logger.info("Created role: {}", name);
        }
    }

} 