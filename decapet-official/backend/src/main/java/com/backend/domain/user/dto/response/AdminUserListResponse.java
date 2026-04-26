package com.backend.domain.user.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.backend.domain.pet.entity.Gender;
import com.backend.domain.breed.entity.Species;
import com.backend.domain.pet.entity.VetPosition;
import com.backend.domain.user.entity.PermissionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자용 회원 목록 응답")
public record AdminUserListResponse(
		@Schema(description = "회원 ID", example = "01HQXK5V7B2M3N4P5Q6R7S8T9U")
		String id,

		@Schema(description = "고유번호", example = "U-20240115-ABC123")
		String uniqueNumber,

		@Schema(description = "이름", example = "홍길동")
		String name,

		@Schema(description = "이메일", example = "hong@example.com")
		String email,

		@Schema(description = "전화번호", example = "01012345678")
		String phone,

		@Schema(description = "우편번호", example = "06234")
		String zipCode,

		@Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
		String address,

		@Schema(description = "상세주소", example = "101동 1001호")
		String detailAddress,

		@Schema(description = "구매자 등급 (1, 2, 3, 4 또는 null)", example = "1")
		String buyerGrade,

		@Schema(description = "관리자 메모", example = "VIP 고객")
		String adminMemo,

		@Schema(description = "관리자 메모 2", example = "추가 메모")
		String adminMemo2,

		@Schema(description = "권한 목록", example = "[\"PRODUCT_PURCHASE\", \"APPOINTMENT_BOOKING\"]")
		Set<PermissionType> permissions,

		@Schema(description = "가입일", example = "2024-01-15T10:30:00")
		LocalDateTime createdAt,

		@Schema(description = "수정일", example = "2024-03-20T14:20:00")
		LocalDateTime updatedAt,

		@Schema(description = "스팸 전화번호 여부", example = "false")
		boolean isSpamPhone,

		@Schema(description = "반려동물 목록")
		List<PetInfo> pets
) {
	@Schema(description = "반려동물 정보")
	public record PetInfo(
			@Schema(description = "반려동물 ID", example = "01HQXK5V7B2M3N4P5Q6R7S8T9V")
			String id,

			@Schema(description = "고유번호", example = "P-20240115-XYZ789")
			String uniqueNumber,

			@Schema(description = "동물등록번호 (15자리)", example = "410123456789012")
			String registrationNumber,

			@Schema(description = "이름", example = "뽀삐")
			String name,

			@Schema(description = "종류", example = "DOG")
			Species species,

			@Schema(description = "품종", example = "말티즈")
			String breedName,

			@Schema(description = "커스텀 품종", example = "믹스견")
			String customBreed,

			@Schema(description = "성별", example = "MALE")
			Gender gender,

			@Schema(description = "중성화 여부", example = "true")
			Boolean neutered,

			@Schema(description = "생년월일", example = "2020-03-15")
			LocalDate birthdate,

			@Schema(description = "나이", example = "5")
			Integer age,

			@Schema(description = "체중 (kg)", example = "4.5")
			BigDecimal weight,

			@Schema(description = "사진 URL")
			String photoUrl,

			@Schema(description = "담당 수의사 목록")
			List<VetInfo> vets
	) {
	}

	@Schema(description = "수의사 정보")
	public record VetInfo(
			@Schema(description = "병원명", example = "서울동물병원")
			String hospitalName,

			@Schema(description = "수의사 이름", example = "김수의")
			String vetName,

			@Schema(description = "직책", example = "DIRECTOR")
			VetPosition vetPosition
	) {
	}
}
