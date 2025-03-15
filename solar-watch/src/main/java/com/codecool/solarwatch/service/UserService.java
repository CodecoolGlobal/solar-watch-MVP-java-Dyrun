package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.entity.Member;
import com.codecool.solarwatch.model.entity.Role;
import com.codecool.solarwatch.model.entity.RoleType;
import com.codecool.solarwatch.repository.MemberRepository;
import com.codecool.solarwatch.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.logging.Logger;

@Service
public class UserService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final Logger logger = Logger.getLogger(UserService.class.getName());

    public UserService(MemberRepository memberRepository, RoleRepository roleRepository) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    public void createUser(String name, String password) {
        Member user = new Member();
        Role roleUser = roleRepository.findByRoleType(RoleType.ROLE_USER).orElseThrow();
        user.setName(name);
        user.setPassword(password);
        user.setRoles(Set.of(roleUser));
        memberRepository.save(user);
    }

    public Member addAdminToUser(String username) {
        logger.info("Adding admin to user: " + username);
        Member user = memberRepository.findByName(username).orElseThrow();
        Role roleAdmin = roleRepository.findByRoleType(RoleType.ROLE_ADMIN).orElseThrow();
        user.getRoles().add(roleAdmin);
        return memberRepository.save(user);
    }
}
