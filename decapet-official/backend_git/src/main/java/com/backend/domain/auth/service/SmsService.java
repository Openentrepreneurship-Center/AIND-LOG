package com.backend.domain.auth.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.domain.auth.exception.SmsSendFailedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SnsClient snsClient;

    private static final String SMS_MESSAGE_TEMPLATE = "[데카펫] 인증번호는 [%s]입니다. 5분 내에 입력해주세요.";

    public void sendVerificationCode(String phone, String code) {
        String message = String.format(SMS_MESSAGE_TEMPLATE, code);
        String e164Phone = formatToE164(phone);

        log.info("SMS 발송 시작 - 전화번호: ***{}", phone.length() > 4 ? phone.substring(phone.length() - 4) : phone);

        try {
            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(e164Phone)
                    .message(message)
                    .messageAttributes(Map.of(
                            "AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                                    .stringValue("Transactional")
                                    .dataType("String")
                                    .build(),
                            "AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                                    .stringValue("DECAPET")
                                    .dataType("String")
                                    .build()
                    ))
                    .build();

            log.info("AWS SNS Publish 요청 시작");
            PublishResponse response = snsClient.publish(request);
            log.info("SMS 발송 성공 - MessageId: {}, StatusCode: {}",
                    response.messageId(), response.sdkHttpResponse().statusCode());
        } catch (Exception e) {
            log.error("SMS 발송 실패 - 전화번호: ***{}, 에러: {}", e164Phone.length() > 4 ? e164Phone.substring(e164Phone.length() - 4) : e164Phone, e.getMessage(), e);
            throw new SmsSendFailedException();
        }
    }

    private String formatToE164(String phone) {
        if (phone.startsWith("+")) {
            return phone;
        }
        if (phone.startsWith("0")) {
            return "+82" + phone.substring(1);
        }
        return "+82" + phone;
    }
}
