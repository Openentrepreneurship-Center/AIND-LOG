package com.backend.domain.user.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.admin.entity.AdminRole;
import com.backend.domain.admin.repository.AdminRepository;
import com.backend.domain.auth.repository.RefreshTokenRepository;
import com.backend.domain.cart.service.CartService;
import com.backend.domain.medicinecart.service.MedicineCartService;
import com.backend.domain.spam.entity.SpamPhonePattern;
import com.backend.domain.spam.repository.SpamPhonePatternRepository;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.repository.PetRepository;
import com.backend.domain.user.dto.mapper.AdminUserResponseMapper;
import com.backend.domain.user.dto.request.BulkUpdatePermissionsRequest;
import com.backend.domain.user.dto.request.UpdatePermissionsRequest;
import com.backend.domain.user.dto.response.AdminUserListResponse;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.event.UserDeletedEvent;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.aop.AdminAudit;
import com.backend.global.common.PageResponse;
import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminRepository adminRepository;
    private final AdminUserResponseMapper adminUserResponseMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;
    private final MedicineCartService medicineCartService;
    private final SpamPhonePatternRepository spamPhonePatternRepository;

    public PageResponse<AdminUserListResponse> getUsers(String searchText, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<User> spec = buildSpecification(searchText, startDate, endDate);
        Page<User> users = userRepository.findAll(spec, pageable);

        List<String> userIds = users.getContent().stream()
            .map(User::getId)
            .toList();

        List<Pet> petsWithVets = userRepository.findPetsWithVetsByUserIds(userIds);

        Map<String, List<Pet>> petsByUserId = petsWithVets.stream()
            .collect(Collectors.groupingBy(p -> p.getUser().getId()));

        List<SpamPhonePattern> spamPatterns = spamPhonePatternRepository.findAll();

        return PageResponse.from(
            users.map(user -> adminUserResponseMapper.toResponse(
                user,
                petsByUserId.getOrDefault(user.getId(), List.of()),
                isSpamPhone(user.getPhone(), spamPatterns)
            ))
        );
    }

    private boolean isSpamPhone(String phone, List<SpamPhonePattern> patterns) {
        if (phone == null || phone.isBlank() || patterns.isEmpty()) {
            return false;
        }
        String normalized = phone.replaceAll("[^0-9]", "");
        return patterns.stream().anyMatch(p -> normalized.contains(p.getPattern()));
    }

    @AdminAudit(action = "USER_UPDATE_ADMIN_INFO", targetType = "User")
    @Transactional
    public void updateUserAdminInfo(String userId, String buyerGrade, String adminMemo, String adminMemo2) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        if (buyerGrade != null) {
            user.updateBuyerGrade(buyerGrade);
        }
        if (adminMemo != null) {
            user.updateAdminMemo(adminMemo);
        }
        if (adminMemo2 != null) {
            user.updateAdminMemo2(adminMemo2);
        }
    }

    @AdminAudit(action = "USER_UPDATE_PERMISSIONS", targetType = "User")
    @Transactional
    public void updatePermissions(String userId, UpdatePermissionsRequest request) {
        requireSuperAdmin();
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        var oldPermissions = new java.util.HashSet<>(user.getPermissions());
        user.updatePermissions(request.permissions());

        if (oldPermissions.contains(PermissionType.PRODUCT_PURCHASE)
                && !request.permissions().contains(PermissionType.PRODUCT_PURCHASE)) {
            cartService.clearCart(userId);
        }
        if (oldPermissions.contains(PermissionType.APPOINTMENT_BOOKING)
                && !request.permissions().contains(PermissionType.APPOINTMENT_BOOKING)) {
            medicineCartService.clearCart(userId);
        }

        refreshTokenRepository.deleteByUserId(userId);
    }

    @AdminAudit(action = "USER_BULK_UPDATE_PERMISSIONS", targetType = "User")
    @Transactional
    public void bulkUpdatePermissions(BulkUpdatePermissionsRequest request) {
        requireSuperAdmin();

        List<User> users = userRepository.findAllById(request.userIds());
        for (User user : users) {
            var oldPermissions = new java.util.HashSet<>(user.getPermissions());
            user.updatePermissions(request.permissions());

            if (oldPermissions.contains(PermissionType.PRODUCT_PURCHASE)
                    && !request.permissions().contains(PermissionType.PRODUCT_PURCHASE)) {
                cartService.clearCart(user.getId());
            }
            if (oldPermissions.contains(PermissionType.APPOINTMENT_BOOKING)
                    && !request.permissions().contains(PermissionType.APPOINTMENT_BOOKING)) {
                medicineCartService.clearCart(user.getId());
            }

            refreshTokenRepository.deleteByUserId(user.getId());
        }
    }

    @AdminAudit(action = "USER_DELETE", targetType = "User")
    @Transactional
    public void deleteUser(String userId) {
        requireSuperAdmin();
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        // 즉시 처리: 토큰 무효화 + PII 익명화 + soft delete
        refreshTokenRepository.deleteByUserId(userId);
        user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.delete();

        // 비동기 cascade 삭제 (트랜잭션 커밋 후 실행)
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
    }

    private Specification<User> buildSpecification(String searchText, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }
            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";
                var petJoin = root.join("pets", JoinType.LEFT);
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(root.get("phone"), like),
                    cb.like(cb.lower(root.get("uniqueNumber")), like),
                    cb.like(cb.lower(petJoin.get("name")), like)
                ));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void requireSuperAdmin() {
        String adminId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Admin admin = adminRepository.findByIdOrThrow(adminId);
        if (admin.getRole() != AdminRole.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

}
