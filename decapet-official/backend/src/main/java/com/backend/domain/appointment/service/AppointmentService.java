package com.backend.domain.appointment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.global.common.PageResponse;

import com.backend.domain.appointment.dto.mapper.AppointmentMapper;
import com.backend.domain.appointment.dto.mapper.AppointmentMedicineItemMapper;
import com.backend.domain.appointment.dto.mapper.AppointmentResponseMapper;
import com.backend.domain.appointment.dto.internal.CreateAppointmentCommand;
import com.backend.domain.appointment.dto.request.CreateAppointmentRequest;
import com.backend.domain.appointment.dto.response.AppointmentResponse;
import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.appointment.entity.AppointmentMedicineItem;
import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.exception.AppointmentNotFoundException;
import com.backend.domain.appointment.exception.DuplicateUserAppointmentException;
import com.backend.domain.appointment.exception.QuestionnaireValidationException;
import com.backend.domain.appointment.exception.TimeSlotFullException;
import com.backend.domain.appointment.repository.AppointmentRepository;
import com.backend.domain.appointment.dto.internal.AppointmentSearchCondition;
import com.backend.domain.medicinecart.entity.MedicineCart;
import com.backend.domain.medicinecart.exception.MedicineCartEmptyException;
import com.backend.domain.medicinecart.service.MedicineCartService;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.exception.PetPermissionDeniedException;
import com.backend.domain.pet.service.PetService;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.schedule.repository.TimeSlotRepository;
import com.backend.domain.schedule.service.ScheduleService;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED);

    private final AppointmentRepository appointmentRepository;
    private final ScheduleService scheduleService;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentMedicineItemMapper appointmentMedicineItemMapper;
    private final AppointmentResponseMapper appointmentResponseMapper;
    private final MedicineCartService medicineCartService;
    private final PetService petService;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentResponse createAppointment(String userId, CreateAppointmentRequest request) {
        Pet pet = petService.getPetEntity(request.petId());

        if (pet.getUser() == null || !pet.getUser().getId().equals(userId)) {
            throw new PetPermissionDeniedException();
        }

        // 비관적 락으로 TimeSlot 조회 (race condition 방지)
        TimeSlot timeSlot = timeSlotRepository.findByIdForUpdateOrThrow(request.timeSlotId());

        // TimeSlot 유효성 검증 (과거 일정 차단)
        scheduleService.validateTimeSlotNotExpired(timeSlot);

        // 반려동물 단위 중복 예약 체크 (같은 유저+같은 반려동물+같은 타임슬롯 예약 불가)
        if (appointmentRepository.existsByUserIdAndPetIdAndTimeSlotIdAndStatusIn(
                userId, request.petId(), request.timeSlotId(), ACTIVE_STATUSES)) {
            throw new DuplicateUserAppointmentException();
        }

        // 타임슬롯 정원 체크 (유저 단위: 같은 유저의 여러 마리 예약은 1자리)
        boolean userAlreadyBooked = appointmentRepository.existsByUserIdAndTimeSlotIdAndStatusIn(
                userId, request.timeSlotId(), ACTIVE_STATUSES);
        if (!userAlreadyBooked) {
            long currentUserCount = appointmentRepository.countDistinctUserByTimeSlotIdAndStatusIn(
                    request.timeSlotId(), ACTIVE_STATUSES);
            if (timeSlot.isFull((int) currentUserCount)) {
                throw new TimeSlotFullException();
            }
        }

        MedicineCart cart = medicineCartService.getCartEntityForUpdate(userId);

        if (cart.isEmptyForPet(request.petId())) {
            throw new MedicineCartEmptyException();
        }

        if (!cart.hasAllAffirmativeAnswersForPet(request.petId())) {
            throw new QuestionnaireValidationException();
        }

        List<AppointmentMedicineItem> medicines = appointmentMedicineItemMapper.toEntities(cart.getItemsForPet(request.petId()));

        User user = userRepository.getReferenceById(userId);

        CreateAppointmentCommand command = new CreateAppointmentCommand(user, pet, timeSlot);
        Appointment appointment = appointmentMapper.toEntity(command);

        medicines.forEach(appointment::addMedicine);

        Appointment saved = appointmentRepository.save(appointment);

        medicineCartService.clearCartForPet(userId, request.petId());

        return appointmentResponseMapper.toResponse(saved);
    }

	@Transactional(readOnly = true)
	public PageResponse<AppointmentResponse> getUserAppointments(String userId, LocalDateTime fromDate, Pageable pageable) {
		if (fromDate != null) {
			return PageResponse.from(
				appointmentRepository.findByUserIdAndCreatedAtAfter(userId, fromDate, pageable)
					.map(appointmentResponseMapper::toResponse)
			);
		}
		return PageResponse.from(
			appointmentRepository.findByUserId(userId, pageable)
				.map(appointmentResponseMapper::toResponse)
		);
	}

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(String userId, String appointmentId) {
        Appointment appointment = appointmentRepository.getById(appointmentId);
        if (appointment.getUser() == null || !appointment.getUser().getId().equals(userId)) {
            throw new AppointmentNotFoundException();
        }
        return appointmentResponseMapper.toResponse(appointment);
    }

}
