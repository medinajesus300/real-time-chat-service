package com.medina.chat_service.repository;

import com.medina.chat_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique username.
     * @param username the username to search for
     * @return Optional containing the User if found
     */
    Optional<User> findByUsername(String username);


}
