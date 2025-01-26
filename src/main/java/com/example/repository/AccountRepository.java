package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.example.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query(
        value = "SELECT COUNT(*) > 0 AS match_found FROM account WHERE username = :username",
        nativeQuery = true
    )
    boolean isUsernameReserved(@Param("username") String username);
    
}
