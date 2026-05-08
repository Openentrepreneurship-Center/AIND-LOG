package com.backend.domain.medicinecart.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.medicine.dto.internal.QuestionnaireItem;
import com.backend.domain.medicine.entity.Medicine;
import com.backend.domain.medicine.service.MedicineService;
import com.backend.domain.medicinecart.dto.internal.AddMedicineCommand;
import com.backend.domain.medicinecart.dto.internal.AddPrescriptionCommand;
import com.backend.domain.medicinecart.dto.internal.UpdateQuantityCommand;
import com.backend.domain.medicinecart.dto.mapper.MedicineCartItemMapper;
import com.backend.domain.medicinecart.dto.mapper.MedicineCartResponseMapper;
import com.backend.domain.medicinecart.dto.request.QuestionnaireAnswerRequest;
import com.backend.domain.medicinecart.dto.response.MedicineCartResponse;
import com.backend.domain.medicinecart.entity.MedicineCart;
import com.backend.domain.medicinecart.entity.MedicineCartItem;
import com.backend.domain.medicinecart.entity.MedicineItemType;
import com.backend.domain.medicinecart.exception.DifferentTimeSlotException;
import com.backend.domain.medicinecart.exception.PrescriptionQuantityNotChangeableException;
import com.backend.domain.medicinecart.exception.QuestionnaireNotCompletedException;
import com.backend.domain.medicinecart.repository.MedicineCartRepository;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.exception.PetPermissionDeniedException;
import com.backend.domain.pet.service.PetService;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.exception.PrescriptionPermissionDeniedException;
import com.backend.domain.prescription.exception.PrescriptionValidationException;
import com.backend.domain.prescription.repository.PrescriptionRepository;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.schedule.service.ScheduleService;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicineCartService {

    private final MedicineCartRepository cartRepository;
    private final MedicineService medicineService;
    private final PetService petService;
    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineCartResponseMapper cartResponseMapper;
    private final MedicineCartItemMapper cartItemMapper;

    @Transactional
    public MedicineCartResponse getCart(String userId) {
        MedicineCart cart = getOrCreateCart(userId);
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public MedicineCartResponse addMedicine(String userId, AddMedicineCommand command) {
        // 반려동물 소유권 검증
        validatePetOwnership(userId, command.petId());

        // TimeSlot 유효성 검증 (과거 일정 차단)
        TimeSlot timeSlot = scheduleService.getTimeSlot(command.timeSlotId());
        scheduleService.validateTimeSlotNotExpired(timeSlot);

        MedicineCart cart = getOrCreateCart(userId);

        // TimeSlot 검증: timeSlotId가 없으면 설정, 있으면 일치 여부 확인
        if (cart.getTimeSlotId() == null) {
            cart.setTimeSlotId(command.timeSlotId());
        } else if (!command.timeSlotId().equals(cart.getTimeSlotId())) {
            throw new DifferentTimeSlotException();
        }

        Pet pet = petService.getPetEntity(command.petId());
        Medicine medicine = medicineService.getMedicineEntity(command.medicineId());

        validateQuestionnaireAnswers(medicine, command.questionnaireAnswers());

        MedicineCartItem item = cartItemMapper.toEntity(
                medicine,
                command.quantity(),
                pet.getId(),
                pet.getName(),
                command.questionnaireAnswers()
        );
        cart.addItem(item);
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public MedicineCartResponse addPrescription(String userId, AddPrescriptionCommand command) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(command.prescriptionId());

        // 본인 처방인지 확인
        if (!prescription.getUser().getId().equals(userId)) {
            throw new PrescriptionPermissionDeniedException();
        }

        // 승인된 처방인지 확인
        if (prescription.getStatus() != PrescriptionStatus.APPROVED) {
            throw new PrescriptionValidationException("승인된 처방만 장바구니에 담을 수 있습니다.");
        }

        // 가격 설정 확인
        if (prescription.getPrice() == null) {
            throw new PrescriptionValidationException("가격이 설정되지 않은 처방입니다.");
        }

        // TimeSlot 유효성 검증 (과거 일정 차단)
        TimeSlot timeSlot = scheduleService.getTimeSlot(command.timeSlotId());
        scheduleService.validateTimeSlotNotExpired(timeSlot);

        MedicineCart cart = getOrCreateCart(userId);

        // TimeSlot 검증: timeSlotId가 없으면 설정, 있으면 일치 여부 확인
        if (cart.getTimeSlotId() == null) {
            cart.setTimeSlotId(command.timeSlotId());
        } else if (!command.timeSlotId().equals(cart.getTimeSlotId())) {
            throw new DifferentTimeSlotException();
        }

        MedicineCartItem item = cartItemMapper.toPrescriptionEntity(prescription);
        cart.addItem(item);
        return cartResponseMapper.toResponse(cart);
    }

    private void validateQuestionnaireAnswers(Medicine medicine, List<QuestionnaireAnswerRequest> answers) {
        List<QuestionnaireItem> questionnaire = medicine.getQuestionnaire();

        // 문진 항목이 없으면 검증 skip
        if (questionnaire == null || questionnaire.isEmpty()) {
            return;
        }

        // 응답이 없거나 비어있으면 에러
        if (answers == null || answers.isEmpty()) {
            throw new QuestionnaireNotCompletedException();
        }

        // 모든 문진 항목에 대해 응답이 있는지 확인
        Set<String> requiredQuestionIds = questionnaire.stream()
                .map(QuestionnaireItem::getQuestionId)
                .collect(Collectors.toSet());

        Set<String> answeredQuestionIds = answers.stream()
                .map(QuestionnaireAnswerRequest::questionId)
                .collect(Collectors.toSet());

        if (!answeredQuestionIds.containsAll(requiredQuestionIds)) {
            throw new QuestionnaireNotCompletedException();
        }
    }

    @Transactional
    public MedicineCartResponse updateQuantity(UpdateQuantityCommand command) {
        if (command.itemType() == MedicineItemType.PRESCRIPTION) {
            throw new PrescriptionQuantityNotChangeableException();
        }
        validatePetOwnership(command.userId(), command.petId());
        MedicineCart cart = getOrCreateCart(command.userId());
        cart.updateItemQuantity(command.itemType(), command.petId(), command.itemId(), command.quantity());
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public MedicineCartResponse removeMedicine(String userId, MedicineItemType itemType, String petId, String itemId) {
        validatePetOwnership(userId, petId);
        MedicineCart cart = getOrCreateCart(userId);
        cart.removeItem(itemType, petId, itemId);
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        MedicineCart cart = getOrCreateCart(userId);
        cart.clear();
    }

    @Transactional
    public void clearCartForPet(String userId, String petId) {
        MedicineCart cart = getOrCreateCart(userId);
        cart.clearItemsForPet(petId);
    }

    @Transactional
    public MedicineCart getCartEntity(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Transactional
    public MedicineCart getCartEntityForUpdate(String userId) {
        return cartRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private MedicineCart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    MedicineCart newCart = createNewCart(userId);
                    return cartRepository.save(newCart);
                });
    }

    private MedicineCart createNewCart(String userId) {
        User user = userRepository.getReferenceById(userId);
        return MedicineCart.builder()
                .user(user)
                .build();
    }

    private void validatePetOwnership(String userId, String petId) {
        Pet pet = petService.getPetEntity(petId);
        if (pet.getUser() == null || !pet.getUser().getId().equals(userId)) {
            throw new PetPermissionDeniedException();
        }
    }
}
