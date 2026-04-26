package com.backend.domain.user.dto.mapper;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.entity.PetVet;
import com.backend.domain.user.dto.response.AdminUserListResponse;
import com.backend.domain.user.dto.response.AdminUserListResponse.PetInfo;
import com.backend.domain.user.dto.response.AdminUserListResponse.VetInfo;
import com.backend.domain.user.entity.User;
import com.backend.global.util.DateTimeUtil;

@Component
public class AdminUserResponseMapper {

	public AdminUserListResponse toResponse(User user, List<Pet> petsWithVets, boolean isSpamPhone) {
		List<PetInfo> petInfos = petsWithVets.stream()
				.map(this::toPetInfo)
				.toList();

		return new AdminUserListResponse(
				user.getId(),
				user.getUniqueNumber(),
				user.getName(),
				user.getEmail(),
				user.getPhone(),
				user.getZipCode(),
				user.getAddress(),
				user.getDetailAddress(),
				user.getBuyerGrade(),
				user.getAdminMemo(),
				user.getAdminMemo2(),
				user.getPermissions(),
				user.getCreatedAt(),
				user.getUpdatedAt(),
				isSpamPhone,
				petInfos
		);
	}

	private PetInfo toPetInfo(Pet pet) {
		List<VetInfo> vetInfos = pet.getVets().stream()
				.map(this::toVetInfo)
				.toList();

		return new PetInfo(
				pet.getId(),
				pet.getUniqueNumber(),
				pet.getRegistrationNumber(),
				pet.getName(),
				pet.getBreed() != null ? pet.getBreed().getSpecies() : null,
				pet.getBreedName(),
				pet.getCustomBreed(),
				pet.getGender(),
				pet.getNeutered(),
				pet.getBirthdate(),
				calculateAge(pet.getBirthdate()),
				pet.getWeight(),
				pet.getPhoto(),
				vetInfos
		);
	}

	private VetInfo toVetInfo(PetVet petVet) {
		return new VetInfo(
				petVet.getHospitalName(),
				petVet.getVetName(),
				petVet.getVetPosition()
		);
	}

	private Integer calculateAge(LocalDate birthdate) {
		if (birthdate == null) {
			return null;
		}
		return Period.between(birthdate, DateTimeUtil.today()).getYears();
	}
}
