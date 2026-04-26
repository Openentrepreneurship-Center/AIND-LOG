package com.backend.global.util;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.backend.global.error.exception.InvalidInputException;
import com.backend.global.error.exception.ValidationFailedException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonParseUtil {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public <T> T parse(String json, Class<T> clazz) {
        try {
            T result = objectMapper.readValue(json, clazz);
            validate(result);
            return result;
        } catch (ValidationFailedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("JSON 파싱 실패 - class: {}", clazz.getSimpleName(), e);
            throw new InvalidInputException();
        }
    }

    private <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new ValidationFailedException(message);
        }
    }
}
