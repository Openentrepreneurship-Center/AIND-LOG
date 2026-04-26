package com.backend.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.user.entity.UserTermConsent;
import com.backend.domain.user.entity.UserTermConsent.UserTermConsentId;

public interface UserTermConsentRepository extends JpaRepository<UserTermConsent, UserTermConsentId> {

    @Query("SELECT t FROM UserTermConsent t WHERE t.user.id = :userId")
    List<UserTermConsent> findByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM UserTermConsent t WHERE t.user.id = :userId AND t.id.termId = :termId")
    Optional<UserTermConsent> findByUserIdAndTermId(@Param("userId") String userId, @Param("termId") String termId);
}
