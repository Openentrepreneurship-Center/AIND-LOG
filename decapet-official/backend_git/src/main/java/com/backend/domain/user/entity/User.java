package com.backend.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.SQLRestriction;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.user.dto.internal.ProfileUpdateInfo;
import com.backend.global.common.BaseEntity;
import com.backend.global.util.UniqueNumberGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final int LOCK_DURATION_MINUTES = 15;

	@Column(nullable = false, unique = true, length = 12)
	private String uniqueNumber;

	@Column(nullable = false, length = 100)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 11)
	private String phone;

	@Column(nullable = false, length = 10)
	private String name;

	@Column(nullable = false, length = 5)
	private String zipCode;

	@Column(nullable = false, length = 100)
	private String address;

	@Column(length = 100)
	private String detailAddress;

	@Column(nullable = false, length = 10)
	private String recipientName;

	@Column(nullable = false, length = 11)
	private String recipientPhone;

	@Column(length = 1)
	private String buyerGrade;

	@Column(columnDefinition = "TEXT")
	private String adminMemo;

	@Column(columnDefinition = "TEXT")
	private String adminMemo2;

	@Column(nullable = false)
	private int failedLoginAttempts;

	private LocalDateTime lockedUntil;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	private Set<PermissionType> permissions = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<UserTermConsent> terms = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private Set<Pet> pets = new HashSet<>();

	@Builder
	public User(String email, String password, String phone, String name, String zipCode, String address, String detailAddress) {
		this.uniqueNumber = UniqueNumberGenerator.generate();
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.name = name;
		this.zipCode = zipCode;
		this.address = address;
		this.detailAddress = detailAddress;
		this.recipientName = name;
		this.recipientPhone = phone;
		this.permissions = new HashSet<>();
		this.terms = new ArrayList<>();
	}

	public void updateBuyerGrade(String buyerGrade) {
		this.buyerGrade = buyerGrade;
	}

	public void updateAdminMemo(String adminMemo) {
		this.adminMemo = adminMemo;
	}

	public void updateAdminMemo2(String adminMemo2) {
		this.adminMemo2 = adminMemo2;
	}

	public void updatePermissions(Set<PermissionType> permissions) {
		this.permissions.clear();
		if (permissions != null) {
			this.permissions.addAll(permissions);
		}
	}

	public void updateProfile(ProfileUpdateInfo info) {
		if (info.name() != null) {
			this.name = info.name();
		}
		// phone 변경은 SMS 인증 플로우를 통해서만 허용 (V-02-1)
		if (info.zipCode() != null) {
			this.zipCode = info.zipCode();
		}
		if (info.address() != null) {
			this.address = info.address();
		}
		if (info.detailAddress() != null) {
			this.detailAddress = info.detailAddress();
		}
		if (info.recipientName() != null) {
			this.recipientName = info.recipientName();
		}
		if (info.recipientPhone() != null) {
			this.recipientPhone = info.recipientPhone();
		}
	}

	public void addTermConsent(UserTermConsent consent) {
		consent.setUser(this);
		this.terms.add(consent);
	}

	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void regenerateUniqueNumber() {
		this.uniqueNumber = UniqueNumberGenerator.generate();
	}

	public boolean isLocked() {
		return lockedUntil != null && DateTimeUtil.now().isBefore(lockedUntil);
	}

	public void recordLoginFailure() {
		this.failedLoginAttempts++;
		if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
			this.lockedUntil = DateTimeUtil.now().plusMinutes(LOCK_DURATION_MINUTES);
		}
	}

	public void resetLoginAttempts() {
		this.failedLoginAttempts = 0;
		this.lockedUntil = null;
	}

	public void anonymize(String anonymizedPassword) {
		this.email = "withdrawn_" + this.getId() + "@deleted.local";
		this.password = anonymizedPassword;
		this.phone = "00000000000";
		this.name = "탈퇴회원";
		this.zipCode = "00000";
		this.address = "삭제됨";
		this.detailAddress = null;
		this.recipientName = "탈퇴회원";
		this.recipientPhone = "00000000000";
	}
}
