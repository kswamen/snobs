package com.back.snobs.domain.mailverification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailVerificationRepository extends JpaRepository<MailVerification, Long> {
    Optional<MailVerification> findBySnob_UserEmail(String userEmail);
}