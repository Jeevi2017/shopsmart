package com.shopsmart.config; 

import com.shopsmart.entity.Role;
import com.shopsmart.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        
        if (roleRepository.findByName("ROLE_CUSTOMER").isEmpty()) {
            Role customerRole = new Role();
            customerRole.setName("ROLE_CUSTOMER");
            roleRepository.save(customerRole);
            System.out.println("Seeded ROLE_CUSTOMER");
        }

        
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
            System.out.println("Seeded ROLE_ADMIN");
        }
        
        if ( roleRepository.findByName("ROLE_SUPER_ADMIN").isEmpty()) {
        	Role superAdminRole =  new Role();
        	superAdminRole.setName("ROLE_SUPER_ADMIN");
        	roleRepository.save(superAdminRole);
        	System.out.println("Seeded ROLE_SUPER_ADMIN");
        }
    }
}