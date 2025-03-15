package com.codecool.solarwatch.configuration;

import com.codecool.solarwatch.model.entity.Member;
import com.codecool.solarwatch.model.entity.Role;
import com.codecool.solarwatch.model.entity.RoleType;
import com.codecool.solarwatch.repository.MemberRepository;
import com.codecool.solarwatch.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DatabaseInitializer {

    @Bean
    public CommandLineRunner initRolesAndUsers(RoleRepository roleRepo, MemberRepository memberRepo, PasswordEncoder encoder) {
        return args -> {
            if (roleRepo.findByRoleType(RoleType.ROLE_USER).isEmpty()) {
                Role roleUser = new Role();
                roleUser.setRoleType(RoleType.ROLE_USER);
                Role roleAdmin = new Role();
                roleAdmin.setRoleType(RoleType.ROLE_ADMIN);
                roleRepo.save(roleUser);
                roleRepo.save(roleAdmin);
                if (memberRepo.findByName("admin").isEmpty()) {
                    Member admin = new Member();
                    admin.setName("admin");
                    admin.setPassword(encoder.encode("admin"));
                    admin.setRoles(Set.of(roleAdmin, roleUser));
                    memberRepo.save(admin);
                }
            }
        };
    }
}
