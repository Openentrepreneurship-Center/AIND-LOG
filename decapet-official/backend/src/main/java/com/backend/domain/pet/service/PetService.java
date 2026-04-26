package com.backend.domain.pet.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.breed.entity.Breed;
import com.backend.domain.breed.repository.BreedRepository;
import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.appointment.repository.AppointmentRepository;
import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.customproduct.repository.CustomProductRepository;
import com.backend.domain.medicinecart.repository.MedicineCartRepository;
import com.backend.domain.pet.dto.internal.PetRegisterInfo;
import com.backend.domain.pet.dto.internal.PetUpdateInfo;
import com.backend.domain.pet.dto.request.PetVetRequest;
import com.backend.domain.pet.dto.request.PetVetUpdateRequest;
import com.backend.domain.pet.dto.response.PetResponse;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.entity.PetVet;
import com.backend.domain.pet.exception.InvalidBirthdateException;
import com.backend.domain.pet.exception.PetNotFoundException;
import com.backend.domain.pet.exception.WeightUpdateRestrictedException;
import com.backend.domain.pet.mapper.PetMapper;
import com.backend.domain.pet.mapper.PetResponseMapper;
import com.backend.domain.pet.mapper.PetVetMapper;
import com.backend.domain.pet.repository.PetRepository;
import com.backend.domain.pet.repository.PetVetRepository;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.prescription.repository.PrescriptionRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.backend.global.util.DateTimeUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private static final int MAX_BIRTHDATE_YEARS_AGO = 50;
    private static final String PET_PHOTO_DIRECTORY = "pets";

    private final PetRepository petRepository;
    private final PetVetRepository petVetRepository;
    private final UserRepository userRepository;
    private final BreedRepository breedRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final CustomProductRepository customProductRepository;
    private final MedicineCartRepository medicineCartRepository;
    private final PetMapper petMapper;
    private final PetResponseMapper petResponseMapper;
    private final PetVetMapper petVetMapper;
    private final S3Service s3Service;

    @Transactional
    public PetResponse registerPet(PetRegisterInfo info) {
        validateBirthdate(info.birthdate());

        User user = userRepository.getByIdAndDeletedAtIsNull(info.userId());
        Breed breed = breedRepository.getById(info.breedId());

        Pet pet = petMapper.toEntity(info, user, breed);

        if (info.photo() != null && !info.photo().isEmpty()) {
            String photoUrl = s3Service.uploadFile(info.photo(), PET_PHOTO_DIRECTORY);
            pet.updatePhoto(photoUrl);
        }

        // Vet 정보 추가
        if (info.vets() != null && !info.vets().isEmpty()) {
            for (PetVetRequest vetRequest : info.vets()) {
                PetVet petVet = petVetMapper.toEntity(vetRequest, pet);
                pet.addVet(petVet);
            }
        }

        petRepository.save(pet);
        return petResponseMapper.toResponse(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getUserPets(String userId) {
        return petRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(petResponseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetResponse getPet(String userId, String petId) {
        Pet pet = petRepository.getByIdAndUserId(petId, userId);
        return petResponseMapper.toResponse(pet);
    }

    @Transactional
    public PetResponse updatePet(PetUpdateInfo info) {
        Pet pet = petRepository.getByIdAndUserId(info.petId(), info.userId());

        if (info.birthdate() != null) {
            validateBirthdate(info.birthdate());
        }

        Breed breed = info.breedId() != null
                ? breedRepository.getReferenceById(info.breedId())
                : null;

        pet.updateInfo(
                info.name(),
                breed,
                info.customBreed(),
                info.gender(),
                info.neutered() != null ? info.neutered() : pet.getNeutered(),
                info.birthdate(),
                info.registrationNumber()
        );

        if (info.weight() != null) {
            if (!pet.canUpdateWeight()) {
                throw new WeightUpdateRestrictedException();
            }
            pet.updateWeight(info.weight());
        }

        if (info.photo() != null && !info.photo().isEmpty()) {
            String photoUrl = s3Service.uploadFile(info.photo(), PET_PHOTO_DIRECTORY);
            pet.updatePhoto(photoUrl);
        }

        // Vet 정보 처리 (추가/수정/삭제)
        if (info.vets() != null) {
            processVetUpdates(pet, info.vets());
        }

        return petResponseMapper.toResponse(pet);
    }

    private void processVetUpdates(Pet pet, List<PetVetUpdateRequest> vetRequests) {
        for (PetVetUpdateRequest vetRequest : vetRequests) {
            if (vetRequest.id() == null) {
                // 신규 추가
                PetVet newVet = PetVet.builder()
                        .pet(pet)
                        .hospitalName(vetRequest.hospitalName())
                        .vetName(vetRequest.vetName())
                        .vetPosition(vetRequest.vetPosition())
                        .build();
                pet.addVet(newVet);
            } else if (vetRequest.deleted()) {
                // 삭제
                PetVet existingVet = petVetRepository.getByIdAndPetId(vetRequest.id(), pet.getId());
                pet.removeVet(existingVet);
                petVetRepository.delete(existingVet);
            } else {
                // 수정
                PetVet existingVet = petVetRepository.getByIdAndPetId(vetRequest.id(), pet.getId());
                existingVet.update(vetRequest.hospitalName(), vetRequest.vetName(), vetRequest.vetPosition());
            }
        }
    }

    @Transactional
    public void deletePet(String userId, String petId) {
        Pet pet = petRepository.getByIdAndUserId(petId, userId);

        // Cascade: Appointment + AppointmentMedicineItem (hard delete via orphanRemoval)
        appointmentRepository.findByPetIdIncludingDeleted(petId)
                .forEach(appointment -> {
                    appointment.getMedicines().clear();
                    appointment.delete();
                });

        // Cascade: Prescription 익명화 + soft delete
        prescriptionRepository.findByPetIdIncludingDeleted(petId)
                .forEach(prescription -> {
                    prescription.anonymize();
                    prescription.delete();
                });

        // Cascade: CustomProduct 익명화 + soft delete
        customProductRepository.findByPetIdIncludingDeleted(petId)
                .forEach(customProduct -> {
                    customProduct.anonymize();
                    customProduct.delete();
                });

        // PetVet soft delete
        pet.getVets().forEach(PetVet::delete);

        // MedicineCart에서 해당 Pet 아이템 제거
        medicineCartRepository.findByUserId(userId)
                .ifPresent(cart -> cart.clearItemsForPet(petId));

        // Pet 익명화 + soft delete
        pet.anonymize();
        pet.delete();
    }

    private void validateBirthdate(LocalDate birthdate) {
        if (birthdate.isAfter(DateTimeUtil.today())) {
            throw new InvalidBirthdateException();
        }
        if (birthdate.isBefore(DateTimeUtil.today().minusYears(MAX_BIRTHDATE_YEARS_AGO))) {
            throw new InvalidBirthdateException();
        }
    }


    @Transactional(readOnly = true)
    public Pet getPetEntity(String petId) {
        return petRepository.findById(petId)
                .orElseThrow(PetNotFoundException::new);
    }
}
