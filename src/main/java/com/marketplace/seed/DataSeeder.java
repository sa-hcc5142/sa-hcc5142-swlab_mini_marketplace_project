package com.marketplace.seed;

import com.marketplace.entity.Role;
import com.marketplace.repository.RoleRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        List<String> roles = List.of("ADMIN", "SELLER", "BUYER");
        for (String roleName : roles) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                return roleRepository.save(role);
            });
        }
    }
}
