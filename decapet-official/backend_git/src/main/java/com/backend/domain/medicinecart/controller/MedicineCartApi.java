package com.backend.domain.medicinecart.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.medicinecart.dto.request.AddMedicineRequest;
import com.backend.domain.medicinecart.dto.request.AddPrescriptionRequest;
import com.backend.domain.medicinecart.dto.request.UpdateQuantityRequest;
import com.backend.domain.medicinecart.entity.MedicineItemType;
import com.backend.global.common.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "의약품 장바구니", description = "의약품 장바구니 (사용자당 1개, 기본 의약품 + 처방전)")
public interface MedicineCartApi {

    @Operation(
        summary = "의약품 장바구니 조회",
        description = """
            사용자의 의약품 장바구니를 조회합니다.

            **비즈니스 로직**
            - 사용자당 하나의 의약품 장바구니
            - 기본 의약품(MEDICINE)과 처방전(PRESCRIPTION) 모두 포함
            - 각 아이템에 반려동물 정보(petId, petName) 포함
            - 총 금액 및 수량 자동 계산

            **응답 정보**
            - 장바구니에 담긴 모든 아이템 목록 (반려동물별로 구분)
            - 각 의약품의 설문 응답 정보 (처방전은 null)
            - 총 금액 및 총 수량

            **사용 화면**: 장바구니 페이지 (출장진료 섹션)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "장바구니 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "MC001",
                          "httpStatus": "OK",
                          "message": "의약품 장바구니 조회 성공",
                          "data": {
                            "id": "medicart123",
                            "items": [
                              {
                                "itemType": "MEDICINE",
                                "itemId": "med123",
                                "itemName": "심장사상충 예방약",
                                "petId": "pet123",
                                "petName": "뽀삐",
                                "unitPrice": 30000,
                                "quantity": 1,
                                "subtotal": 30000,
                                "imageUrl": "https://example.com/med.jpg",
                                "questionnaireAnswers": []
                              },
                              {
                                "itemType": "PRESCRIPTION",
                                "itemId": "presc123",
                                "itemName": "처방 의약품",
                                "petId": "pet123",
                                "petName": "뽀삐",
                                "unitPrice": 50000,
                                "quantity": 1,
                                "subtotal": 50000,
                                "imageUrl": null,
                                "questionnaireAnswers": null
                              }
                            ],
                            "totalAmount": 80000,
                            "totalItemCount": 2,
                            "empty": false
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getCart(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId
    );

    @Operation(
        summary = "의약품 추가",
        description = """
            장바구니에 기본 의약품을 추가합니다.

            **비즈니스 로직**
            - 반려동물 ID를 request body에 포함
            - 같은 반려동물의 같은 의약품이 있으면 수량 증가
            - 다른 반려동물이면 별도 아이템으로 추가

            **제약사항**
            - 수량은 1 이상 필수
            - 반려동물 소유권 검증
            - 필수 설문이 있는 경우 응답 제공 필수

            **사용 화면**: 의약품 상세 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "의약품 추가 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> addMedicine(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Valid AddMedicineRequest request
    );

    @Operation(
        summary = "처방전 추가",
        description = """
            장바구니에 승인된 처방전을 추가합니다.

            **비즈니스 로직**
            - 승인(APPROVED)된 처방전만 추가 가능
            - 처방전의 TimeSlot과 장바구니 TimeSlot이 일치해야 함
            - 처방전은 수량 1로 고정

            **제약사항**
            - 본인의 처방전만 추가 가능
            - 가격이 설정되어 있어야 함
            - TimeSlot이 만료되지 않아야 함

            **사용 화면**: 의약품 조회 페이지 (내 처방 탭)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처방전 추가 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (미승인 처방, 가격 미설정 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 처방)"),
        @ApiResponse(responseCode = "404", description = "처방전을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> addPrescription(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Valid AddPrescriptionRequest request
    );

    @Operation(
        summary = "아이템 수량 변경",
        description = """
            장바구니 내 아이템 수량을 변경합니다.

            **비즈니스 로직**
            - itemType + petId + itemId 조합으로 아이템 식별
            - 수량 변경 시 총 금액 자동 재계산

            **사용 화면**: 장바구니 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수량 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> updateQuantity(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "아이템 타입", required = false, example = "MEDICINE")
        MedicineItemType itemType,
        @Parameter(description = "반려동물 ID", required = true, example = "pet123")
        String petId,
        @Parameter(description = "아이템 ID (의약품 ID 또는 처방전 ID)", required = true, example = "med123")
        String itemId,
        @Valid UpdateQuantityRequest request
    );

    @Operation(
        summary = "아이템 삭제",
        description = """
            장바구니에서 아이템을 삭제합니다.

            **비즈니스 로직**
            - itemType + petId + itemId 조합으로 아이템 식별 및 삭제
            - 삭제 후 총 금액 자동 재계산

            **사용 화면**: 장바구니 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "아이템 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> removeMedicine(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "아이템 타입", required = false, example = "MEDICINE")
        MedicineItemType itemType,
        @Parameter(description = "반려동물 ID", required = true, example = "pet123")
        String petId,
        @Parameter(description = "아이템 ID (의약품 ID 또는 처방전 ID)", required = true, example = "med123")
        String itemId
    );

    @Operation(
        summary = "장바구니 비우기",
        description = """
            사용자의 의약품 장바구니를 모두 비웁니다.

            **비즈니스 로직**
            - 모든 반려동물의 모든 아이템을 일괄 삭제

            **사용 화면**: 장바구니 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장바구니 비우기 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> clearCart(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId
    );
}
