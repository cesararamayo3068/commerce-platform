package com.portfolio.commerce.security;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AuthAccountRepository extends JpaRepository<AuthAccount,Long> { Optional<AuthAccount> findByEmailIgnoreCase(String email); boolean existsByEmailIgnoreCase(String email); }
