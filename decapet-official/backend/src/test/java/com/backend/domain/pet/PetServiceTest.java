package com.backend.domain.pet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.backend.domain.breed.entity.Breed;
import com.backend.domain.breed.entity.Species;
import com.backend.domain.pet.dto.request.PetRegisterRequest;
import com.backend.domain.pet.entity.Gender;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import jakarta.servlet.http.Cookie;

@DisplayName("Pet API 통합 테스트")
class PetServiceTest extends IntegrationTestBase {

    private Breed createBreed() {
        Breed breed = Breed.builder()
                .species(Species.DOG)
                .name("골든 리트리버")
                .build();
        em.persist(breed);
        em.flush();
        return breed;
    }

    private Pet createPet(User user, Breed breed) {
        Pet pet = Pet.builder()
                .user(user)
                .name("뽀삐")
                .breed(breed)
                .gender(Gender.MALE)
                .neutered(true)
                .birthdate(LocalDate.of(2020, 3, 15))
                .weight(new BigDecimal("5.50"))
                .build();
        em.persist(pet);
        em.flush();
        return pet;
    }

    @Nested
    @DisplayName("POST /api/v1/pets - 반려동물 등록")
    class RegisterPet {

        @Test
        @DisplayName("유효한 요청으로 반려동물 등록 성공")
        void registerPetSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);
            Breed breed = createBreed();

            PetRegisterRequest request = new PetRegisterRequest(
                    "뽀삐", breed.getId(), null, Gender.MALE,
                    true, LocalDate.of(2020, 3, 15),
                    new BigDecimal("5.50"), null, null
            );

            String json = objectMapper.writeValueAsString(request);
            MockMultipartFile dataPart = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

            mockMvc.perform(multipart("/api/v1/pets")
                            .file(dataPart)
                            .cookie(cookie)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("뽀삐"));
        }

        @Test
        @DisplayName("인증 없이 등록 시 401")
        void registerPetWithoutAuth() throws Exception {
            mockMvc.perform(multipart("/api/v1/pets")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pets - 반려동물 목록 조회")
    class GetMyPets {

        @Test
        @DisplayName("반려동물 목록 조회 성공")
        void getMyPetsSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);
            Breed breed = createBreed();
            createPet(user, breed);

            mockMvc.perform(get("/api/v1/pets").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 목록 조회 시 401")
        void getMyPetsWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/pets"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pets/{petId} - 반려동물 상세 조회")
    class GetPet {

        @Test
        @DisplayName("반려동물 상세 조회 성공")
        void getPetSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);
            Breed breed = createBreed();
            Pet pet = createPet(user, breed);

            mockMvc.perform(get("/api/v1/pets/{petId}", pet.getId()).cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("뽀삐"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/pets/{petId} - 반려동물 삭제")
    class DeletePet {

        @Test
        @DisplayName("반려동물 삭제 성공")
        void deletePetSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);
            Breed breed = createBreed();
            Pet pet = createPet(user, breed);

            mockMvc.perform(delete("/api/v1/pets/{petId}", pet.getId()).cookie(cookie))
                    .andExpect(status().isOk());
        }
    }
}
