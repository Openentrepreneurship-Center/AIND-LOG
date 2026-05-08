package com.backend.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.auth.exception.InvalidAccountOrPhoneException;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.exception.DuplicateEmailException;
import com.backend.domain.user.exception.DuplicatePhoneException;
import com.backend.domain.user.exception.UserNotFoundException;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = {"pets", "permissions"})
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.vets WHERE p.user.id IN :userIds AND p.deletedAt IS NULL")
    List<Pet> findPetsWithVetsByUserIds(@Param("userIds") List<String> userIds);

    Optional<User> findByEmail(String email);

	default User getByEmail(String email) {
		return findByEmail(email).orElseThrow(UserNotFoundException::new);
	}

	Optional<User> findByIdAndDeletedAtIsNull(String id);

	default User getByIdAndDeletedAtIsNull(String id) {
		return findByIdAndDeletedAtIsNull(id)
			.orElseThrow(UserNotFoundException::new);
	}

    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    default User getByPhone(String phone) {
        return findByPhoneAndDeletedAtIsNull(phone)
            .orElseThrow(UserNotFoundException::new);
    }

    Optional<User> findByEmailAndPhoneAndDeletedAtIsNull(String email, String phone);

    default User getByEmailAndPhone(String email, String phone) {
        return findByEmailAndPhoneAndDeletedAtIsNull(email, phone)
            .orElseThrow(InvalidAccountOrPhoneException::new);
    }

    default void validateEmailNotDuplicate(String email) {
        if (existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException();
        }
    }

    default void validatePhoneNotDuplicate(String phone) {
        if (existsByPhoneAndDeletedAtIsNull(phone)) {
            throw new DuplicatePhoneException();
        }
    }

    default void validatePhoneNotDuplicate(String phone, String currentPhone) {
        if (phone != null && !phone.equals(currentPhone)) {
            if (existsByPhoneAndDeletedAtIsNull(phone)) {
                throw new DuplicatePhoneException();
            }
        }
    }

    @Modifying
    @Query(value = "DELETE FROM user_permissions WHERE user_id = :userId", nativeQuery = true)
    void deletePermissionsByUserId(@Param("userId") String userId);
}
