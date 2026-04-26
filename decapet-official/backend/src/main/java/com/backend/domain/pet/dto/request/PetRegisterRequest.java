package com.backend.domain.pet.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.backend.domain.pet.entity.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

@Schema(description = "반려동물 등록 요청")
public record PetRegisterRequest(
        @Schema(description = "반려동물 이름 (최대 50자)", example = "뽀삐")
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name,

        @Schema(description = "품종 ID (품종 API에서 조회)", example = "507f1f77bcf86cd799439011")
        @NotBlank(message = "품종을 선택해주세요.")
        String breedId,

        @Schema(description = "직접 입력 품종명 (기타 선택 시)", example = "믹스견")
        @Size(max = 100, message = "품종명은 100자를 초과할 수 없습니다.")
        String customBreed,

        @Schema(description = "성별 (MALE 또는 FEMALE)", example = "MALE")
        @NotNull(message = "성별을 선택해주세요.")
        Gender gender,

        @Schema(description = "중성화 여부", example = "true")
        boolean neutered,

        @Schema(description = "생년월일 (YYYY-MM-DD)", example = "2020-03-15")
        @NotNull(message = "생년월일을 입력해주세요.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthdate,

        @Schema(description = "체중 (0.1~100kg)", example = "5.5")
        @NotNull(message = "체중을 입력해주세요.")
        @DecimalMin(value = "0.1", message = "체중은 0.1kg 이상이어야 합니다.")
        @DecimalMax(value = "100.0", message = "체중은 100kg 이하여야 합니다.")
        BigDecimal weight,

        @Schema(description = "동물등록번호 (15자리 숫자, 선택사항)", example = "410123456789012")
        @jakarta.validation.constraints.Pattern(regexp = "^\\d{15}$", message = "동물등록번호는 15자리 숫자여야 합니다.")
        String registrationNumber,

        @Schema(description = "수의사 정보 목록 (선택사항)")
        @Valid
        List<PetVetRequest> vets
) {
}
