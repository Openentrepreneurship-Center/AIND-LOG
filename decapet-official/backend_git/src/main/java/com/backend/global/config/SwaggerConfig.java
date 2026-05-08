package com.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfig {

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;

	@Bean
	public OpenAPI openAPI() {
		String serverUrl = "prod".equals(activeProfile)
			? "https://prod.api.decapet.co.kr"
			: "https://dev.api.decapet.co.kr";
		String serverDescription = "prod".equals(activeProfile) ? "Prod Server" : "Dev Server";

		return new OpenAPI()
			.addServersItem(new Server().url(serverUrl).description(serverDescription))
			.info(new Info()
				.title("Decapet API")
				.version("1.0.0"))
			// User APIs
			.addTagsItem(new Tag().name("인증").description("로그인, 회원가입, SMS 인증"))
			.addTagsItem(new Tag().name("사용자").description("프로필 관리"))
			.addTagsItem(new Tag().name("반려동물").description("반려동물 등록 및 관리"))
			.addTagsItem(new Tag().name("장바구니").description("상품 장바구니"))
			.addTagsItem(new Tag().name("의약품 장바구니").description("의약품 장바구니"))
			.addTagsItem(new Tag().name("상품").description("상품 조회"))
			.addTagsItem(new Tag().name("맞춤 상품").description("맞춤 상품 신청"))
			.addTagsItem(new Tag().name("의약품").description("의약품 조회"))
			.addTagsItem(new Tag().name("주문").description("주문 관리"))
			.addTagsItem(new Tag().name("결제").description("결제 처리"))
			.addTagsItem(new Tag().name("배송").description("배송 조회"))
			.addTagsItem(new Tag().name("예약").description("진료 예약"))
			.addTagsItem(new Tag().name("스케줄").description("예약 가능 시간 조회"))
			.addTagsItem(new Tag().name("처방").description("처방 관리"))
			.addTagsItem(new Tag().name("게시판").description("게시글 조회 및 작성"))
			.addTagsItem(new Tag().name("배너").description("배너 조회"))
			.addTagsItem(new Tag().name("약관").description("약관 조회 및 동의"))
			.addTagsItem(new Tag().name("품종").description("품종 조회"))
			// Admin APIs
			.addTagsItem(new Tag().name("[Admin] 인증").description("관리자 로그인"))
			.addTagsItem(new Tag().name("[Admin] 사용자").description("사용자 관리"))
			.addTagsItem(new Tag().name("[Admin] 상품").description("상품 관리"))
			.addTagsItem(new Tag().name("[Admin] 맞춤 상품").description("맞춤 상품 관리"))
			.addTagsItem(new Tag().name("[Admin] 의약품").description("의약품 관리"))
			.addTagsItem(new Tag().name("[Admin] 배송").description("배송 관리"))
			.addTagsItem(new Tag().name("[Admin] 예약").description("예약 관리"))
			.addTagsItem(new Tag().name("[Admin] 스케줄").description("스케줄 관리"))
			.addTagsItem(new Tag().name("[Admin] 처방").description("처방 관리"))
			.addTagsItem(new Tag().name("[Admin] 게시판").description("게시판 관리"))
			.addTagsItem(new Tag().name("[Admin] 배너").description("배너 관리"))
			.addTagsItem(new Tag().name("[Admin] 약관").description("약관 관리"));
	}
}
