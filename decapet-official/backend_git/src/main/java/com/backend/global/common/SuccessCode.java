package com.backend.global.common;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // Auth
    SMS_SEND_SUCCESS(HttpStatus.OK, "A001", "인증 코드가 발송되었습니다."),
    SMS_VERIFY_SUCCESS(HttpStatus.OK, "A002", "인증이 완료되었습니다."),
    REGISTER_SUCCESS(HttpStatus.CREATED, "A003", "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "A004", "로그인에 성공했습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "A005", "토큰이 갱신되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "A006", "로그아웃되었습니다."),
    EMAIL_CHECK_SUCCESS(HttpStatus.OK, "A007", "이메일 중복 확인 완료"),
    PASSWORD_SMS_SEND_SUCCESS(HttpStatus.OK, "A008", "비밀번호 재설정 인증 코드가 발송되었습니다."),
    PASSWORD_SMS_VERIFY_SUCCESS(HttpStatus.OK, "A009", "인증이 완료되었습니다."),
    PASSWORD_RESET_SUCCESS(HttpStatus.OK, "A010", "비밀번호가 변경되었습니다."),

    // User
    USER_GET_SUCCESS(HttpStatus.OK, "U001", "회원 정보 조회 성공"),
    USER_UPDATE_SUCCESS(HttpStatus.OK, "U002", "회원 정보 수정 성공"),
    USER_DELETE_SUCCESS(HttpStatus.OK, "U003", "회원 탈퇴 성공"),
    USER_PHONE_SMS_SEND_SUCCESS(HttpStatus.OK, "U009", "연락처 변경 인증 코드가 발송되었습니다."),
    USER_PHONE_CHANGE_SUCCESS(HttpStatus.OK, "U010", "연락처가 변경되었습니다."),
    USER_PASSWORD_CHANGE_SUCCESS(HttpStatus.OK, "U011", "비밀번호가 변경되었습니다."),
    USER_LIST_SUCCESS(HttpStatus.OK, "U004", "회원 목록 조회 성공"),
    USER_ADMIN_INFO_UPDATE_SUCCESS(HttpStatus.OK, "U006", "회원 관리 정보 수정 성공"),
    USER_PERMISSIONS_UPDATE_SUCCESS(HttpStatus.OK, "U007", "회원 권한 수정 성공"),
    USER_PERMISSIONS_BULK_UPDATE_SUCCESS(HttpStatus.OK, "U009", "회원 권한 일괄 수정 성공"),
    USER_PET_DELETE_SUCCESS(HttpStatus.OK, "U008", "회원 반려동물 삭제 성공"),

    // Pet
    PET_REGISTER_SUCCESS(HttpStatus.CREATED, "P001", "반려동물 등록 성공"),
    PET_GET_SUCCESS(HttpStatus.OK, "P002", "반려동물 정보 조회 성공"),
    PET_UPDATE_SUCCESS(HttpStatus.OK, "P003", "반려동물 정보 수정 성공"),
    PET_DELETE_SUCCESS(HttpStatus.OK, "P004", "반려동물 삭제 성공"),
    PET_LIST_SUCCESS(HttpStatus.OK, "P005", "반려동물 목록 조회 성공"),

    // Breed
    BREED_LIST_SUCCESS(HttpStatus.OK, "BR001", "품종 목록 조회 성공"),

    // Product
    PRODUCT_GET_SUCCESS(HttpStatus.OK, "PR001", "상품 조회 성공"),
    PRODUCT_LIST_SUCCESS(HttpStatus.OK, "PR002", "상품 목록 조회 성공"),
    PRODUCT_CREATE_SUCCESS(HttpStatus.CREATED, "PR004", "상품 생성 성공"),

    // Custom Product
    CUSTOM_PRODUCT_CREATE_SUCCESS(HttpStatus.CREATED, "CP001", "커스텀 상품 신청 성공"),
    CUSTOM_PRODUCT_GET_SUCCESS(HttpStatus.OK, "CP002", "커스텀 상품 조회 성공"),
    CUSTOM_PRODUCT_LIST_SUCCESS(HttpStatus.OK, "CP003", "커스텀 상품 목록 조회 성공"),
    CUSTOM_PRODUCT_APPROVE_SUCCESS(HttpStatus.OK, "CP004", "커스텀 상품 승인 성공"),
    CUSTOM_PRODUCT_REJECT_SUCCESS(HttpStatus.OK, "CP005", "커스텀 상품 거절 성공"),

    // Cart
    CART_GET_SUCCESS(HttpStatus.OK, "C001", "장바구니 조회 성공"),
    CART_ADD_SUCCESS(HttpStatus.OK, "C002", "장바구니 추가 성공"),
    CART_UPDATE_SUCCESS(HttpStatus.OK, "C003", "장바구니 수정 성공"),
    CART_REMOVE_SUCCESS(HttpStatus.OK, "C004", "장바구니 삭제 성공"),
    CART_CLEAR_SUCCESS(HttpStatus.OK, "C005", "장바구니 비우기 성공"),

    // Medicine Cart
    MEDICINE_CART_GET_SUCCESS(HttpStatus.OK, "MC001", "의약품 장바구니 조회 성공"),
    MEDICINE_CART_ADD_SUCCESS(HttpStatus.OK, "MC002", "의약품 장바구니 추가 성공"),
    MEDICINE_CART_UPDATE_SUCCESS(HttpStatus.OK, "MC003", "의약품 장바구니 수정 성공"),
    MEDICINE_CART_REMOVE_SUCCESS(HttpStatus.OK, "MC004", "의약품 장바구니 삭제 성공"),
    MEDICINE_CART_CLEAR_SUCCESS(HttpStatus.OK, "MC005", "의약품 장바구니 비우기 성공"),

    // Order
    ORDER_CREATE_SUCCESS(HttpStatus.CREATED, "O001", "주문 생성 성공"),
    ORDER_GET_SUCCESS(HttpStatus.OK, "O002", "주문 조회 성공"),
    ORDER_LIST_SUCCESS(HttpStatus.OK, "O003", "주문 목록 조회 성공"),
    ORDER_CANCEL_SUCCESS(HttpStatus.OK, "O004", "주문 취소 성공"),
    ORDER_CONFIRM_DELIVERY_SUCCESS(HttpStatus.OK, "O005", "구매 확정 성공"),

    // Delivery
    DELIVERY_GET_SUCCESS(HttpStatus.OK, "D001", "배송 조회 성공"),
    DELIVERY_LIST_SUCCESS(HttpStatus.OK, "D002", "배송 목록 조회 성공"),

    // Schedule
    SCHEDULE_CREATE_SUCCESS(HttpStatus.CREATED, "SC001", "일정 생성 성공"),
    SCHEDULE_GET_SUCCESS(HttpStatus.OK, "SC002", "일정 조회 성공"),
    SCHEDULE_LIST_SUCCESS(HttpStatus.OK, "SC003", "일정 목록 조회 성공"),
    SCHEDULE_DELETE_SUCCESS(HttpStatus.OK, "SC004", "일정 삭제 성공"),
    TIME_SLOT_ADD_SUCCESS(HttpStatus.CREATED, "SC005", "시간대 추가 성공"),
    TIME_SLOT_DELETE_SUCCESS(HttpStatus.OK, "SC006", "시간대 삭제 성공"),

    // Appointment
    APPOINTMENT_REQUEST_SUCCESS(HttpStatus.CREATED, "AP001", "예약 신청 성공"),
    APPOINTMENT_GET_SUCCESS(HttpStatus.OK, "AP004", "예약 조회 성공"),
    APPOINTMENT_LIST_SUCCESS(HttpStatus.OK, "AP005", "예약 목록 조회 성공"),
    APPOINTMENT_CART_GET_SUCCESS(HttpStatus.OK, "AP006", "예약 장바구니 조회 성공"),
    APPOINTMENT_CART_UPDATE_SUCCESS(HttpStatus.OK, "AP007", "예약 장바구니 수정 성공"),

    // Medicine
    MEDICINE_GET_SUCCESS(HttpStatus.OK, "M001", "의약품 조회 성공"),
    MEDICINE_LIST_SUCCESS(HttpStatus.OK, "M002", "의약품 목록 조회 성공"),
    MEDICINE_CREATE_SUCCESS(HttpStatus.CREATED, "M003", "의약품 등록 성공"),
    MEDICINE_UPDATE_SUCCESS(HttpStatus.OK, "M004", "의약품 수정 성공"),
    MEDICINE_DELETE_SUCCESS(HttpStatus.OK, "M005", "의약품 삭제 성공"),

    // Prescription
    PRESCRIPTION_REQUEST_SUCCESS(HttpStatus.CREATED, "RX001", "처방전 요청 성공"),
    PRESCRIPTION_GET_SUCCESS(HttpStatus.OK, "RX002", "처방전 조회 성공"),
    PRESCRIPTION_LIST_SUCCESS(HttpStatus.OK, "RX003", "처방전 목록 조회 성공"),

    // Payment
    PAYMENT_CREATE_SUCCESS(HttpStatus.CREATED, "PM001", "결제 요청 성공"),
    PAYMENT_COMPLETE_SUCCESS(HttpStatus.OK, "PM002", "결제 완료"),
    PAYMENT_CANCEL_SUCCESS(HttpStatus.OK, "PM003", "결제 취소 성공"),
    PAYMENT_GET_SUCCESS(HttpStatus.OK, "PM004", "결제 조회 성공"),
    PAYMENT_LIST_SUCCESS(HttpStatus.OK, "PM005", "결제 목록 조회 성공"),
    ADMIN_PAYMENT_CONFIRM_DEPOSIT_SUCCESS(HttpStatus.OK, "PM006", "입금 확인 완료"),
    ADMIN_PAYMENT_LIST_SUCCESS(HttpStatus.OK, "PM007", "관리자 결제 목록 조회 성공"),

    // Board
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "B001", "게시글 작성 성공"),
    POST_LIST_SUCCESS(HttpStatus.OK, "B002", "게시글 목록 조회 성공"),
    POST_GET_SUCCESS(HttpStatus.OK, "B003", "게시글 조회 성공"),

    // Banner
    BANNER_LIST_SUCCESS(HttpStatus.OK, "BN001", "배너 목록 조회 성공"),

    // Site Setting
    SITE_SETTING_GET_SUCCESS(HttpStatus.OK, "SS001", "사이트 설정 조회 성공"),

    // Remote Area
    REMOTE_AREA_CHECK_SUCCESS(HttpStatus.OK, "RA001", "도서산간 지역 확인 성공"),

    // Terms
    TERM_LIST_SUCCESS(HttpStatus.OK, "T001", "약관 목록 조회 성공"),
    TERM_GET_SUCCESS(HttpStatus.OK, "T002", "약관 조회 성공"),
    TERM_CONSENT_SUCCESS(HttpStatus.OK, "T003", "약관 동의 성공"),

    // Admin Auth
    ADMIN_LOGIN_STEP1_SUCCESS(HttpStatus.OK, "AD001", "1단계 인증 성공, OTP 인증이 필요합니다."),
    ADMIN_LOGIN_SUCCESS(HttpStatus.OK, "AD002", "관리자 로그인 성공"),
    ADMIN_OTP_SETUP_SUCCESS(HttpStatus.OK, "AD003", "OTP 설정 성공"),
    ADMIN_TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "AD004", "관리자 토큰 갱신 성공"),
    ADMIN_LOGOUT_SUCCESS(HttpStatus.OK, "AD005", "관리자 로그아웃 성공"),
    ADMIN_PASSWORD_CHANGE_SUCCESS(HttpStatus.OK, "AD006", "비밀번호가 변경되었습니다."),

    // Admin Operations
    ADMIN_PRODUCT_APPROVE_SUCCESS(HttpStatus.OK, "AD020", "상품 승인 성공"),
    ADMIN_PRODUCT_REJECT_SUCCESS(HttpStatus.OK, "AD021", "상품 거절 성공"),
    ADMIN_PRODUCT_REVERT_SUCCESS(HttpStatus.OK, "AD022", "상품 상태 복원 성공"),
    ADMIN_APPOINTMENT_APPROVE_SUCCESS(HttpStatus.OK, "AD030", "예약 승인 성공"),
    ADMIN_APPOINTMENT_REJECT_SUCCESS(HttpStatus.OK, "AD031", "예약 거절 성공"),
    ADMIN_PRESCRIPTION_APPROVE_SUCCESS(HttpStatus.OK, "AD040", "처방전 승인 성공"),
    ADMIN_PRESCRIPTION_REJECT_SUCCESS(HttpStatus.OK, "AD041", "처방전 거절 성공"),
    ADMIN_PRESCRIPTION_REVERT_SUCCESS(HttpStatus.OK, "AD042", "처방전 상태 복원 성공"),
    ADMIN_DELIVERY_CREATE_SUCCESS(HttpStatus.CREATED, "AD050", "배송 생성 성공"),
    ADMIN_DELIVERY_UPDATE_SUCCESS(HttpStatus.OK, "AD051", "배송 상태 수정 성공"),
    ADMIN_POST_RESOLVE_SUCCESS(HttpStatus.OK, "AD060", "게시글 처리 완료"),

    // Admin Order
    ADMIN_ORDER_LIST_SUCCESS(HttpStatus.OK, "OD003", "관리자 주문 목록 조회 성공"),
    ADMIN_ORDER_DETAIL_SUCCESS(HttpStatus.OK, "OD004", "관리자 주문 상세 조회 성공"),

    // Spam Phone
    SPAM_PHONE_LIST_SUCCESS(HttpStatus.OK, "SP001", "스팸 번호 패턴 목록 조회 성공"),
    SPAM_PHONE_ADD_SUCCESS(HttpStatus.CREATED, "SP002", "스팸 번호 패턴 등록 성공"),
    SPAM_PHONE_DELETE_SUCCESS(HttpStatus.OK, "SP003", "스팸 번호 패턴 삭제 성공"),

    // Admin CRUD
    ADMIN_CREATE_SUCCESS(HttpStatus.CREATED, "AD100", "생성 성공"),
    ADMIN_UPDATE_SUCCESS(HttpStatus.OK, "AD101", "수정 성공"),
    ADMIN_DELETE_SUCCESS(HttpStatus.OK, "AD102", "삭제 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}