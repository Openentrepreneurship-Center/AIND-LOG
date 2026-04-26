package com.backend.global.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Global
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "G001", "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "G003", "잘못된 입력값입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "G004", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "G008", "요청한 리소스를 찾을 수 없습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A004", "토큰이 만료되었습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "A005", "인증 코드가 유효하지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "A006", "인증 코드가 만료되었습니다."),
    VERIFICATION_TOKEN_USED(HttpStatus.BAD_REQUEST, "A007", "이미 사용된 인증 토큰입니다."),
    VERIFICATION_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "A008", "인증 토큰을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "A009", "비밀번호가 올바르지 않습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A010", "SMS 발송에 실패했습니다."),
    TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "A011", "보안 알림: 다시 로그인해주세요."),
    TOKEN_BINDING_MISMATCH(HttpStatus.UNAUTHORIZED, "A012", "새 기기가 감지되었습니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A013", "세션이 만료되었습니다. 다시 로그인해주세요."),
    HTTPS_REQUIRED(HttpStatus.FORBIDDEN, "A014", "보안 연결이 필요합니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "A015", "세션이 종료되었습니다. 다시 로그인해주세요."),
    INVALID_ACCOUNT_OR_PHONE(HttpStatus.NOT_FOUND, "A016", "계정 또는 전화번호 정보가 올바르지 않습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 등록된 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "U003", "이미 등록된 전화번호입니다."),
    USER_NOT_APPROVED(HttpStatus.FORBIDDEN, "U004", "승인되지 않은 사용자입니다."),
    USER_REJECTED(HttpStatus.FORBIDDEN, "U005", "가입이 거절된 사용자입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "U006", "유효하지 않은 사용자 상태입니다."),
    PRODUCT_PURCHASE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "U007", "제품 구매 권한이 없습니다."),
    APPOINTMENT_BOOKING_NOT_ALLOWED(HttpStatus.FORBIDDEN, "U008", "진료 예약 권한이 없습니다."),
    INFORMATION_SHARING_NOT_ALLOWED(HttpStatus.FORBIDDEN, "U009", "정보 공유 권한이 없습니다."),
    REPORT_CENTER_NOT_ALLOWED(HttpStatus.FORBIDDEN, "U010", "신고센터 권한이 없습니다."),
    UNIQUE_NUMBER_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U011", "고유번호 생성에 실패했습니다."),
    USER_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "U012", "계정이 일시적으로 잠겼습니다. 잠시 후 다시 시도해 주세요."),

    // Breed
    BREED_NOT_FOUND(HttpStatus.NOT_FOUND, "BR001", "품종을 찾을 수 없습니다."),

    // Pet
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "반려동물을 찾을 수 없습니다."),
    WEIGHT_UPDATE_RESTRICTED(HttpStatus.BAD_REQUEST, "P002", "체중은 30일에 한 번만 수정할 수 있습니다."),
    INVALID_BIRTHDATE(HttpStatus.BAD_REQUEST, "P003", "생년월일이 유효하지 않습니다."),
    INVALID_WEIGHT(HttpStatus.BAD_REQUEST, "P004", "체중은 0.1kg에서 100kg 사이여야 합니다."),
    PET_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "P005", "해당 반려동물에 대한 권한이 없습니다."),
    PET_VET_NOT_FOUND(HttpStatus.NOT_FOUND, "P006", "수의사 정보를 찾을 수 없습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PR002", "재고가 부족합니다."),
    IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "PR006", "이미지는 필수입니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PR007", "구매할 수 없는 상품입니다."),
    PRODUCT_MULTIPLE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PR008", "이 상품은 수량 추가가 불가합니다."),

    // Custom Product
    CUSTOM_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CP001", "커스텀 상품을 찾을 수 없습니다."),
    CUSTOM_PRODUCT_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "CP002", "이미 승인된 신청입니다."),
    CUSTOM_PRODUCT_ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "CP003", "이미 거절된 신청입니다."),
    CUSTOM_PRODUCT_NOT_APPROVED(HttpStatus.BAD_REQUEST, "CP004", "승인되지 않은 커스텀 상품입니다."),
    CUSTOM_PRODUCT_EXPIRED(HttpStatus.BAD_REQUEST, "CP005", "유효기간이 만료된 커스텀 상품입니다."),
    CUSTOM_PRODUCT_NOT_OWNED(HttpStatus.FORBIDDEN, "CP006", "본인의 커스텀 상품만 구매할 수 있습니다."),
    CUSTOM_PRODUCT_MULTIPLE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CP007", "이 커스텀 상품은 수량 추가가 불가합니다."),

    // Cart
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "장바구니 항목을 찾을 수 없습니다."),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "C002", "장바구니가 비어있습니다."),

    // Medicine Cart
    MEDICINE_CART_NOT_FOUND(HttpStatus.NOT_FOUND, "MC001", "의약품 장바구니를 찾을 수 없습니다."),
    MEDICINE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "MC002", "구매할 수 없는 의약품입니다."),
    MEDICINE_CART_EMPTY(HttpStatus.BAD_REQUEST, "MC003", "장바구니가 비어있습니다. 의약품을 선택해주세요."),
    QUESTIONNAIRE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "MC004", "모든 문진 항목에 응답해주세요."),
    DIFFERENT_TIME_SLOT(HttpStatus.CONFLICT, "MC005", "장바구니에 다른 예약 일정의 의약품이 있습니다."),
    PRESCRIPTION_QUANTITY_NOT_CHANGEABLE(HttpStatus.BAD_REQUEST, "MC006", "처방전 아이템의 수량은 변경할 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O003", "유효하지 않은 주문 상태입니다."),
    ORDER_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "O004", "취소할 수 없는 주문입니다."),
    ORDER_NOT_PENDING(HttpStatus.BAD_REQUEST, "O005", "결제 대기 중인 주문이 아닙니다."),

    // Delivery
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "배송을 찾을 수 없습니다."),
    DUPLICATE_TRACKING_NUMBER(HttpStatus.CONFLICT, "D002", "중복된 운송장 번호입니다."),
    INVALID_DELIVERY_STATUS(HttpStatus.BAD_REQUEST, "D003", "유효하지 않은 배송 상태입니다."),
    TRACKING_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "D004", "운송장 번호가 할당되지 않아 배송완료 처리할 수 없습니다."),

    // Appointment
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "AP001", "예약 일정을 찾을 수 없습니다."),
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "AP002", "예약을 찾을 수 없습니다."),
    DUPLICATE_APPOINTMENT(HttpStatus.CONFLICT, "AP003", "이미 해당 일정에 예약이 있습니다."),
    DUPLICATE_SCHEDULE(HttpStatus.CONFLICT, "AP009", "해당 날짜에 이미 일정이 존재합니다."),
    DUPLICATE_TIME_SLOT(HttpStatus.CONFLICT, "AP010", "해당 시간대가 이미 존재합니다."),
    QUESTIONNAIRE_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "AP004", "설문 응답이 유효하지 않습니다."),
    APPOINTMENT_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "AP005", "이미 승인된 예약입니다."),
    APPOINTMENT_ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "AP006", "이미 거절된 예약입니다."),
    TIME_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "AP007", "예약 시간대를 찾을 수 없습니다."),
    APPOINTMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "AP008", "이미 완료된 예약입니다."),
    TIME_SLOT_EXPIRED(HttpStatus.BAD_REQUEST, "AP011", "선택한 일정이 이미 지났습니다."),
    TIME_SLOT_FULL(HttpStatus.CONFLICT, "AP012", "해당 시간대의 예약이 마감되었습니다."),
    DUPLICATE_USER_APPOINTMENT(HttpStatus.CONFLICT, "AP013", "이미 해당 시간대에 예약이 있습니다."),
    INVALID_SCHEDULE_DATE(HttpStatus.BAD_REQUEST, "SC007", "과거 날짜에는 일정을 생성할 수 없습니다."),

    // Medicine
    MEDICINE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "의약품을 찾을 수 없습니다."),
    WEIGHT_RANGE_MISMATCH(HttpStatus.BAD_REQUEST, "M002", "반려동물 체중이 의약품 적용 범위에 맞지 않습니다."),
    MEDICINE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "M003", "의약품 구매 권한이 없습니다."),
    MONTHS_PER_UNIT_REQUIRED(HttpStatus.BAD_REQUEST, "M004", "기생충예방 타입은 처방 개월 수가 필수입니다."),
    MONTHS_PER_UNIT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "M005", "기생충예방 타입이 아닌 경우 처방 개월 수를 입력할 수 없습니다."),

    // Prescription
    PRESCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "RX001", "처방전을 찾을 수 없습니다."),
    PRESCRIPTION_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "RX002", "이미 승인된 처방전입니다."),
    PRESCRIPTION_ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "RX003", "이미 거절된 처방전입니다."),
    PRESCRIPTION_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "RX004", "해당 처방전에 대한 권한이 없습니다."),
    PRESCRIPTION_INVALID_INPUT(HttpStatus.BAD_REQUEST, "RX005", "처방전 입력이 유효하지 않습니다."),

    // Board
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "게시글을 찾을 수 없습니다."),
    POST_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "B002", "게시글 권한이 없습니다."),
    POST_ALREADY_RESOLVED(HttpStatus.BAD_REQUEST, "B003", "이미 처리된 게시글입니다."),
    POST_NOT_EDITABLE(HttpStatus.BAD_REQUEST, "B007", "수정할 수 없는 게시글입니다."),
    ATTACHMENT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "B004", "첨부파일은 최대 5개까지 가능합니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "B005", "파일 크기는 10MB를 초과할 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "B006", "허용되지 않는 파일 형식입니다."),

    // Banner
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "BN001", "배너를 찾을 수 없습니다."),
    INVALID_BANNER_DATE_RANGE(HttpStatus.BAD_REQUEST, "BN002", "종료 일시는 시작 일시 이후여야 합니다."),
    BANNER_REORDER_INCOMPLETE(HttpStatus.BAD_REQUEST, "BN003", "모든 배너의 순서를 지정해야 합니다."),

    // Spam
    DUPLICATE_SPAM_PATTERN(HttpStatus.CONFLICT, "SP001", "이미 등록된 스팸 패턴입니다."),

    // Site Setting
    SITE_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "SS001", "사이트 설정을 찾을 수 없습니다."),

    // Terms
    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "약관을 찾을 수 없습니다."),
    DUPLICATE_TERM_VERSION(HttpStatus.CONFLICT, "T002", "이미 존재하는 약관 버전입니다."),
    INVALID_EFFECTIVE_DATE(HttpStatus.BAD_REQUEST, "T003", "시행일은 기존 최신 버전의 시행일 이후여야 합니다."),
    INACTIVE_TERM(HttpStatus.BAD_REQUEST, "T004", "현재 유효하지 않은 약관입니다."),
    REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "T005", "필수 약관에 동의해주세요."),

    // Admin
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "AD001", "관리자를 찾을 수 없습니다."),
    INVALID_OTP(HttpStatus.UNAUTHORIZED, "AD002", "OTP 코드가 유효하지 않습니다."),
    OTP_NOT_ENABLED(HttpStatus.BAD_REQUEST, "AD003", "OTP가 설정되지 않았습니다."),
    INVALID_ADMIN_PASSWORD(HttpStatus.UNAUTHORIZED, "AD004", "비밀번호가 올바르지 않습니다."),
    INVALID_TEMP_TOKEN(HttpStatus.UNAUTHORIZED, "AD005", "임시 토큰이 유효하지 않거나 만료되었습니다."),
    ADMIN_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AD006", "계정이 일시적으로 잠겼습니다. 잠시 후 다시 시도해 주세요."),

    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "파일 삭제에 실패했습니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PM001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PM002", "이미 결제가 진행 중입니다."),
    PAYMENT_NOT_PENDING(HttpStatus.BAD_REQUEST, "PM003", "대기 중인 결제가 아닙니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PM004", "결제에 실패했습니다."),
    APPOINTMENT_NOT_APPROVED(HttpStatus.BAD_REQUEST, "PM005", "승인된 예약만 결제할 수 있습니다."),
    PRESCRIPTION_NOT_APPROVED(HttpStatus.BAD_REQUEST, "PM006", "승인된 처방전만 결제할 수 있습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PM007", "결제 금액이 일치하지 않습니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "PM008", "PG사 결제 승인에 실패했습니다."),
    INVALID_PRESCRIPTION_PRICE(HttpStatus.BAD_REQUEST, "PM009", "처방전 가격은 0보다 커야 합니다."),
    TIMESLOT_HAS_APPOINTMENTS(HttpStatus.BAD_REQUEST, "PM010", "해당 시간대에 활성 예약이 있어 삭제할 수 없습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "PM011", "PG사 결제 취소에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}