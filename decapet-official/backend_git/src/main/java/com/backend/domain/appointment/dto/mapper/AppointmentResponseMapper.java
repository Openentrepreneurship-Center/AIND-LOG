package com.backend.domain.appointment.dto.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.dto.response.AppointmentMedicineItemResponse;
import com.backend.domain.appointment.dto.response.AppointmentResponse;
import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.pet.entity.Gender;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.user.entity.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentResponseMapper {

    private final AppointmentMedicineItemResponseMapper appointmentMedicineItemResponseMapper;
    private final PaymentRepository paymentRepository;

    public AppointmentResponse toResponse(Appointment appointment) {
        List<AppointmentMedicineItemResponse> medicines = null;
        if (appointment.getMedicines() != null) {
            medicines = appointment.getMedicines().stream()
                    .map(appointmentMedicineItemResponseMapper::toResponse)
                    .toList();
        }

        TimeSlot timeSlot = appointment.getTimeSlot();

        // soft-deleted User 안전 처리
        String userName = null;
        String userEmail = null;
        String userPhone = null;
        try {
            User user = appointment.getUser();
            if (user != null) {
                userName = user.getName();
                userEmail = user.getEmail();
                userPhone = user.getPhone();
            }
        } catch (EntityNotFoundException e) {
            userName = "(탈퇴한 회원)";
        }

        // soft-deleted Pet 안전 처리
        String petId = null;
        String petName = "(삭제된 반려동물)";
        Gender petGender = null;
        LocalDate petBirthdate = null;
        BigDecimal petWeight = null;
        try {
            Pet pet = appointment.getPet();
            if (pet != null) {
                petId = pet.getId();
                petName = pet.getName();
                petGender = pet.getGender();
                petBirthdate = pet.getBirthdate();
                petWeight = pet.getWeight();
            }
        } catch (EntityNotFoundException e) {
            // 기본값 유지
        }

        // 결제 정보 조회
        String paymentStatus = null;
        String paymentMethod = null;
        String accountHolder = null;
        String cashReceiptType = null;
        String cashReceiptNumber = null;

        Payment payment = paymentRepository.findByAppointmentAndDeletedAtIsNull(appointment.getId())
                .orElse(null);
        if (payment != null) {
            paymentStatus = payment.getStatus().name();
            paymentMethod = payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null;
            accountHolder = payment.getAccountHolder();
            cashReceiptType = payment.getCashReceiptType();
            cashReceiptNumber = payment.getCashReceiptNumber();
        }

        return new AppointmentResponse(
                appointment.getId(),
                timeSlot.getId(),
                userName,
                userEmail,
                userPhone,
                petId,
                petName,
                petGender,
                petBirthdate,
                petWeight,
                timeSlot.getSchedule().getScheduleDate(),
                timeSlot.getTime(),
                timeSlot.getLocation(),
                medicines,
                appointment.getTotalAmount(),
                appointment.getStatus(),
                paymentStatus,
                paymentMethod,
                accountHolder,
                cashReceiptType,
                cashReceiptNumber,
                appointment.getCreatedAt()
        );
    }
}
