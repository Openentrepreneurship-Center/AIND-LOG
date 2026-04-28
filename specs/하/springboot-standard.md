# springboot-standard.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 Spring Boot 4.0.1 / Java 17 / Gradle 프로젝트다.
JPA 로 영속성을 처리하고, JWT 로 인증하며, springdoc 으로 API 를 문서화한다.
이 문서는 프로젝트 참여 시 최소한으로 알아야 할 기술 표준을 정의한다.

---

## 원칙

1. `@SpringBootApplication` 진입점 클래스가 1개 존재한다.
2. 의존성 주입은 생성자 주입을 사용한다.
3. JWT 필터가 Spring Security 필터 체인에 등록되어 있다.
4. Swagger 가 활성화되어 API 문서를 자동으로 생성한다.

---

## 강제 사항

### must

- `@SpringBootApplication` 클래스가 1개 존재해야 한다.
- 의존성 주입은 생성자 주입으로 선언해야 한다 (`@RequiredArgsConstructor` 또는 명시적 생성자).
- JPA(`spring-boot-starter-data-jpa`)를 사용하여 영속성을 처리해야 한다.
- `JwtFilter` 를 Spring Security 필터 체인에 등록해야 한다.
- springdoc OpenAPI 를 활성화하여 Swagger UI 에서 API 목록을 확인할 수 있어야 한다.
- 빌드 및 실행은 `./gradlew build`, `./gradlew bootRun` 명령으로 처리해야 한다.

---

## 예시 (decapet 인용)

### build.gradle — 핵심 의존성

```groovy
// decapet-official/backend/build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.1'
    id 'io.spring.dependency-management' version '1.1.7'
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
    runtimeOnly 'org.postgresql:postgresql'
}
```

### JWT 필터 등록 위치

```java
// com.backend.global.config.SecurityConfig (발췌)
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

### 빌드 및 실행 명령

```bash
./gradlew build
./gradlew bootRun
./gradlew test
```

---

## 체크리스트

- [ ] `@SpringBootApplication` 클래스가 1개다
- [ ] 생성자 주입이 사용되고 있다 (`@Autowired` 필드 주입 없음)
- [ ] JPA 의존성이 `build.gradle` 에 포함되어 있다
- [ ] `JwtFilter` 가 Security 필터 체인에 등록되어 있다
- [ ] Swagger UI 에서 API 목록이 정상 노출된다
- [ ] `./gradlew bootRun` 으로 정상 기동된다
