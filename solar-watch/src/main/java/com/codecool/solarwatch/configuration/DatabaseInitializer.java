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
    public CommandLineRunner initRoles(RoleRepository roleRepo) {
        return args -> {
            if (roleRepo.findByRoleType(RoleType.ROLE_USER).isEmpty()) {
                Role roleUser = new Role();
                roleUser.setRoleType(RoleType.ROLE_USER);
                Role roleAdmin = new Role();
                roleAdmin.setRoleType(RoleType.ROLE_ADMIN);
                roleRepo.save(roleUser);
                roleRepo.save(roleAdmin);
            }
        };
    }

    @Bean
    public CommandLineRunner initUsers(MemberRepository memberRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        return args -> {
            if (memberRepo.findByName("admin").isEmpty()) {
                Member admin = new Member();
                Role roleAdmin = roleRepo.findByRoleType(RoleType.ROLE_ADMIN).orElseThrow();
                Role roleUser = roleRepo.findByRoleType(RoleType.ROLE_USER).orElseThrow();
                admin.setName("admin");
                admin.setPassword(encoder.encode("admin"));
                admin.setRoles(Set.of(roleAdmin, roleUser));
                memberRepo.save(admin);
            }
        };
    }
}
