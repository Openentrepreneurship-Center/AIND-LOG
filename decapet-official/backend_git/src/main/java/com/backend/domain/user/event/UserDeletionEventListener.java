package com.backend.domain.user.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.domain.board.entity.Post;
import com.backend.domain.board.repository.PostRepository;
import com.backend.domain.cart.repository.CartRepository;
import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.delivery.repository.DeliveryRepository;
import com.backend.domain.medicinecart.repository.MedicineCartRepository;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.repository.OrderRepository;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.repository.PetRepository;
import com.backend.domain.pet.service.PetService;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.repository.UserTermConsentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletionEventListener {

    private final PetRepository petRepository;
    private final PetService petService;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final PostRepository postRepository;
    private final CartRepository cartRepository;
    private final MedicineCartRepository medicineCartRepository;
    private final UserTermConsentRepository userTermConsentRepository;
    private final UserRepository userRepository;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserDeleted(UserDeletedEvent event) {
        String userId = event.userId();
        log.info("비동기 cascade 삭제 시작: userId={}", userId);

        // Pet 및 하위 엔티티 (Appointment, Prescription, CustomProduct, PetVet 등)
        try {
            List<Pet> pets = petRepository.findByUserIdOrderByCreatedAtDesc(userId);
            for (Pet pet : pets) {
                petService.deletePet(userId, pet.getId());
            }
        } catch (Exception e) {
            log.error("Pet cascade 삭제 실패: userId={}", userId, e);
        }

        // Payment 익명화 + soft delete (이미 삭제된 레코드 포함)
        try {
            paymentRepository.findAllByUserIdIncludingDeleted(userId)
                    .forEach(payment -> {
                        payment.anonymize();
                        payment.delete();
                    });
        } catch (Exception e) {
            log.error("Payment 삭제 실패: userId={}", userId, e);
        }

        // Delivery PII 익명화 + soft delete (이미 삭제된 레코드 포함)
        try {
            deliveryRepository.findAllByUserIdIncludingDeleted(userId)
                    .forEach(delivery -> {
                        delivery.anonymize();
                        delivery.delete();
                    });
        } catch (Exception e) {
            log.error("Delivery 삭제 실패: userId={}", userId, e);
        }

        // Order: OrderItem hard delete + PII 익명화 + soft delete (이미 삭제된 레코드 포함)
        try {
            orderRepository.findAllByUserIdIncludingDeleted(userId)
                    .forEach(order -> {
                        order.getItems().clear();
                        order.anonymize();
                        order.delete();
                    });
        } catch (Exception e) {
            log.error("Order 삭제 실패: userId={}", userId, e);
        }

        // Post 익명화 + soft delete (이미 삭제된 레코드 포함)
        try {
            postRepository.findAllByUserIdIncludingDeleted(userId)
                    .forEach(post -> {
                        post.anonymize();
                        post.delete();
                    });
        } catch (Exception e) {
            log.error("Post 삭제 실패: userId={}", userId, e);
        }

        // Cart hard delete
        try {
            cartRepository.findByUserId(userId)
                    .ifPresent(cart -> {
                        cart.clear();
                        cartRepository.delete(cart);
                    });
        } catch (Exception e) {
            log.error("Cart 삭제 실패: userId={}", userId, e);
        }

        // MedicineCart hard delete
        try {
            medicineCartRepository.findByUserId(userId)
                    .ifPresent(cart -> {
                        cart.clear();
                        medicineCartRepository.delete(cart);
                    });
        } catch (Exception e) {
            log.error("MedicineCart 삭제 실패: userId={}", userId, e);
        }

        // UserTermConsent hard delete
        try {
            userTermConsentRepository.deleteAll(userTermConsentRepository.findByUserId(userId));
        } catch (Exception e) {
            log.error("UserTermConsent 삭제 실패: userId={}", userId, e);
        }

        // user_permissions hard delete (ElementCollection)
        try {
            userRepository.deletePermissionsByUserId(userId);
        } catch (Exception e) {
            log.error("user_permissions 삭제 실패: userId={}", userId, e);
        }

        log.info("비동기 cascade 삭제 완료: userId={}", userId);
    }
}
