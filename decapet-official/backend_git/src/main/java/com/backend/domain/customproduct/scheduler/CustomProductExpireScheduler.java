package com.backend.domain.customproduct.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.customproduct.entity.CustomProductStatus;
import com.backend.domain.customproduct.repository.CustomProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.backend.global.util.DateTimeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomProductExpireScheduler {

    private final CustomProductRepository customProductRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void expireCustomProducts() {
        try {
            LocalDateTime oneYearAgo = DateTimeUtil.now().minusYears(1);

            List<CustomProduct> expiredProducts = customProductRepository.findExpiredApprovedProducts(
                    CustomProductStatus.APPROVED, oneYearAgo);

            if (!expiredProducts.isEmpty()) {
                for (CustomProduct product : expiredProducts) {
                    product.delete();
                }
                log.info("Expired {} custom products", expiredProducts.size());
            }
        } catch (Exception e) {
            log.error("커스텀 상품 만료 처리 실패", e);
        }
    }
}
