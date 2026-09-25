package com.greenboard.investman.repository.user;

import com.greenboard.investman.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);

    @Query("SELECT DISTINCT u.tenantSchema FROM User u WHERE u.tenantSchema IS NOT NULL")
    List<String> findAllTenantSchemas();
}
