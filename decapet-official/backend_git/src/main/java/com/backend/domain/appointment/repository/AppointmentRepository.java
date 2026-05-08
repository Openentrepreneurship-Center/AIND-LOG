package com.backend.domain.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.exception.AppointmentNotFoundException;
import com.backend.domain.appointment.exception.DuplicateAppointmentException;
import com.backend.domain.appointment.dto.internal.AppointmentSearchCondition;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    @EntityGraph(attributePaths = {"user", "pet", "timeSlot", "timeSlot.schedule"})
    Page<Appointment> findByUserId(String userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "pet", "timeSlot", "timeSlot.schedule"})
    Page<Appointment> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime fromDate, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "pet", "timeSlot", "timeSlot.schedule"})
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "pet", "timeSlot", "timeSlot.schedule"})
    List<Appointment> findByTimeSlotId(String timeSlotId);

    List<Appointment> findByPetId(String petId);

    List<Appointment> findByUserIdAndDeletedAtIsNull(String userId);

    boolean existsByUserIdAndPetIdAndTimeSlotIdAndStatusIn(
            String userId, String petId, String timeSlotId, List<AppointmentStatus> statuses);

    boolean existsByTimeSlotIdAndStatusIn(String timeSlotId, List<AppointmentStatus> statuses);

    long countByTimeSlotIdAndStatusIn(String timeSlotId, List<AppointmentStatus> statuses);

    @Query("SELECT COUNT(DISTINCT a.user.id) FROM Appointment a WHERE a.timeSlot.id = :timeSlotId AND a.status IN :statuses")
    long countDistinctUserByTimeSlotIdAndStatusIn(@Param("timeSlotId") String timeSlotId, @Param("statuses") List<AppointmentStatus> statuses);

    boolean existsByUserIdAndTimeSlotIdAndStatusIn(String userId, String timeSlotId, List<AppointmentStatus> statuses);

    default void validateNotDuplicate(AppointmentSearchCondition condition) {
        if (existsByUserIdAndPetIdAndTimeSlotIdAndStatusIn(
                condition.userId(),
                condition.petId(),
                condition.timeSlotId(),
                condition.statuses()
        )) {
            throw new DuplicateAppointmentException();
        }
    }

	default Appointment getById(String id) {
		return findById(id)
			.orElseThrow(AppointmentNotFoundException::new);
	}

	@Query(value = "SELECT * FROM appointments WHERE pet_id = :petId", nativeQuery = true)
	List<Appointment> findByPetIdIncludingDeleted(@Param("petId") String petId);

	@EntityGraph(attributePaths = {"user", "pet", "timeSlot", "timeSlot.schedule"})
	@Query("SELECT a FROM Appointment a " +
	       "LEFT JOIN a.user u LEFT JOIN a.pet p " +
	       "WHERE (:status IS NULL OR a.status = :status) " +
	       "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
	       "  OR u.phone LIKE CONCAT('%', :keyword, '%') " +
	       "  OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
	       "ORDER BY a.createdAt DESC")
	Page<Appointment> searchByKeyword(@Param("status") AppointmentStatus status,
	                                   @Param("keyword") String keyword,
	                                   Pageable pageable);
}
