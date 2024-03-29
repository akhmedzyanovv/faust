package org.mpi.faust;

import org.mpi.faust.exception.AppException;
import org.mpi.faust.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;



@Component
class Initializer implements CommandLineRunner {

    @Autowired
    PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final AuthorityRepository authorityRepository;

    public Initializer(UserRepository user_repository, AuthorityRepository authorityRepository) {
        this.userRepository = user_repository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    public void run(String... strings) {
        Map<AuthorityType, Authority> authorities = new HashMap<AuthorityType, Authority>();
        Stream.of(AuthorityType.ROLE_EMPEROR, AuthorityType.ROLE_USER, AuthorityType.ROLE_SUPPLIER, AuthorityType.ROLE_TREASURY).forEach(name ->
                authorities.put(name, authorityRepository.save(new Authority(name)))
        );

        List<User> users = new ArrayList<>();

        User emperor = new User();
        emperor.setName("Emperor");
        emperor.setUsername("Emperor");
        emperor.setPassword(passwordEncoder.encode("abc"));
        emperor.setEmail("emp@treasury.com");
        Authority userRole = authorityRepository.findByName(AuthorityType.ROLE_EMPEROR)
                .orElseThrow(() -> new AppException("User Role not set."));
        emperor.setAuthorities(Collections.singleton(userRole));
        users.add(emperor);

        User treasury = new User();
        treasury.setName("Treasury");
        treasury.setUsername("Treasury");
        treasury.setPassword(passwordEncoder.encode("abc"));
        treasury.setEmail("tres@treasury.com");
        treasury.setAuthorities(Collections.singleton(authorities.get(AuthorityType.ROLE_TREASURY)));
        users.add(treasury);

        User supplier = new User();
        supplier.setName("Supplier");
        supplier.setUsername("Supplier");
        supplier.setPassword(passwordEncoder.encode("abc"));
        supplier.setEmail("supploer@treasury.com");
        supplier.setAuthorities(Collections.singleton(authorities.get(AuthorityType.ROLE_SUPPLIER)));
        users.add(supplier);

        User user = new User();
        user.setName("User");
        user.setUsername("User");
        user.setPassword(passwordEncoder.encode("abc"));
        user.setEmail("user@treasury.com");
        user.setAuthorities(Collections.singleton(authorities.get(AuthorityType.ROLE_USER)));
        users.add(user);

        userRepository.saveAll(users);

    }
}