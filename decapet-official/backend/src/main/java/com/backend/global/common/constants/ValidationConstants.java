package com.backend.global.common.constants;

public final class ValidationConstants {

    private ValidationConstants() {
        // 인스턴스화 방지
    }

    public static final String PHONE_REGEX = "^010\\d{8}$";
    public static final String PHONE_MESSAGE = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)";

    public static final String ZIP_CODE_REGEX = "^\\d{5}$";
    public static final String ZIP_CODE_MESSAGE = "우편번호 형식이 올바르지 않습니다. (5자리 숫자)";

    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\x21-\\x7E]+$";
    public static final String PASSWORD_MESSAGE = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.";

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String EMAIL_MESSAGE = "이메일 형식이 올바르지 않습니다.";

    public static final String PET_REGISTRATION_NUMBER_REGEX = "^\\d{15}$";
    public static final String PET_REGISTRATION_NUMBER_MESSAGE = "동물등록번호 형식이 올바르지 않습니다. (15자리 숫자)";

    public static final String OTP_CODE_REGEX = "^\\d{6}$";
    public static final String OTP_CODE_MESSAGE = "인증번호 형식이 올바르지 않습니다. (6자리 숫자)";
}