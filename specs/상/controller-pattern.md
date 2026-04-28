#controller-pattern.md
## Controller 설계 원칙
 
### 1. 단일 책임 원칙
- 하나의 Controller는 하나의 리소스 또는 도메인만 담당
- 비즈니스 로직은 Service 계층에 위임
- Controller는 요청/응답 처리에만 집중
 
### 2. RESTful 설계
- 리소스 중심의 URL 설계
- HTTP 메서드의 올바른 사용
- 상태 코드의 일관된 사용
 
### 3. 검증 및 예외 처리
- 입력 데이터 검증
- 일관된 예외 처리
- 적절한 로깅
## Controller 패턴 구조
 
### 템플릿 기반 구현
이 패턴은 `templates/springboot/controller-template.java` 템플릿을 기반으로 구현됩니다.
 
#### 템플릿 사용 방법
1. 템플릿 파일을 복사하여 새로운 Controller 생성
2. `{Resource}` 플레이스홀더를 실제 리소스명으로 교체
3. `{resource}` 플레이스홀더를 소문자 리소스명으로 교체
4. 필요한 메서드만 선택적으로 구현
 
#### 기본 Controller 구조
```java
// ✅ 템플릿 기반 Controller 패턴
// templates/springboot/controller-template.java 참조
@RestController
@RequestMapping('/api/v1/{resource}s')
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = '{Resource}', description = '{Resource} 관리 API')
public class {Resource}Controller {
     
    private final {Resource}Service {resource}Service;
    private final {Resource}Mapper {resource}Mapper;
     
    // 기본 CRUD 메서드들
    // - get{Resource}(Long id)
    // - get{Resource}List({Resource}SearchRequest request, Pageable pageable)
    // - create{Resource}(Create{Resource}Request request)
    // - update{Resource}(Long id, Update{Resource}Request request)
    // - delete{Resource}(Long id)
     
    // 특화된 메서드들
    // - search{Resource}s({Resource}SearchRequest request)
    // - activate{Resource}(Long id)
    // - deactivate{Resource}(Long id)
}
```
 
### 특화된 Controller 패턴
 
#### 검색 및 필터링 Controller
```java
// ✅ 검색 Controller 패턴
@RestController
@RequestMapping('/api/v1/cards')
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardSearchController {
     
    private final CardService cardService;
    private final CardMapper cardMapper;
     
    @GetMapping('/search')
    public ResponseEntity<ApiResponse<PageResponse<CardResponse>>> searchCards(
            @Valid CardSearchRequest request,
            @PageableDefault(sort = 'createdAt', direction = Sort.Direction.DESC) Pageable pageable) {
         
        log.info('카드 검색 요청: {}', request);
         
        Page<Card> cardPage = cardService.searchCards(request, pageable);
        PageResponse<CardResponse> response = cardMapper.toPageResponse(cardPage);
         
        return ResponseEntity.ok(ApiResponse.success(response));
    }
     
    @GetMapping('/by-user/{userId}')
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCardsByUser(
            @PathVariable @NotNull Long userId) {
         
        log.info('사용자별 카드 조회 요청: {}', userId);
         
        List<Card> cards = cardService.findByUserId(userId);
        List<CardResponse> response = cardMapper.toResponseList(cards);
         
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```
 
#### 상태 변경 Controller
```java
// ✅ 상태 변경 Controller 패턴
@RestController
@RequestMapping('/api/v1/cards')
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardStatusController {
     
    private final CardService cardService;
    private final CardMapper cardMapper;
     
    @PatchMapping('/{cardNumber}/activate')
    public ResponseEntity<ApiResponse<CardResponse>> activateCard(
            @PathVariable @Pattern(regexp = '^[0-9]{16}$') String cardNumber) {
         
        log.info('카드 활성화 요청: {}', maskCardNumber(cardNumber));
         
        Card card = cardService.activateCard(cardNumber);
        CardResponse response = cardMapper.toResponse(card);
         
        return ResponseEntity.ok(ApiResponse.success(response));
    }
     
    @PatchMapping('/{cardNumber}/deactivate')
    public ResponseEntity<ApiResponse<CardResponse>> deactivateCard(
            @PathVariable @Pattern(regexp = '^[0-9]{16}$') String cardNumber) {
         
        log.info('카드 비활성화 요청: {}', maskCardNumber(cardNumber));
         
        Card card = cardService.deactivateCard(cardNumber);
        CardResponse response = cardMapper.toResponse(card);
         
        return ResponseEntity.ok(ApiResponse.success(response));
    }
     
    @PatchMapping('/{cardNumber}/block')
    public ResponseEntity<ApiResponse<CardResponse>> blockCard(
            @PathVariable @Pattern(regexp = '^[0-9]{16}$') String cardNumber,
            @Valid @RequestBody BlockCardRequest request) {
         
        log.info('카드 차단 요청: {}', maskCardNumber(cardNumber));
         
        Card card = cardService.blockCard(cardNumber, request.getReason());
        CardResponse response = cardMapper.toResponse(card);
         
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```
## 어노테이션 사용 패턴
 
### 필수 어노테이션
```java
// ✅ 필수 어노테이션 패턴
@RestController                    // REST API 컨트롤러 선언
@RequestMapping('/api/v1/cards')   // 기본 경로 설정
@Validated                         // 메서드 레벨 검증 활성화
@Slf4j                            // 로깅
@RequiredArgsConstructor          // 생성자 주입
@Tag(name = 'Card', description = '카드 관리 API')  // Swagger 문서화
public class CardController {
    // ...
}
```
 
### 메서드별 어노테이션
```java
// ✅ HTTP 메서드별 어노테이션 패턴
@GetMapping('/{id}')              // 조회
@GetMapping                       // 목록 조회
@PostMapping                      // 생성
@PutMapping('/{id}')             // 전체 수정
@PatchMapping('/{id}')           // 부분 수정
@DeleteMapping('/{id}')          // 삭제
 
// ✅ 파라미터 어노테이션 패턴
@PathVariable @NotNull Long id    // 경로 변수
@RequestParam String status       // 쿼리 파라미터
@RequestBody CreateRequest request // 요청 본문
@PageableDefault Pageable pageable // 페이지네이션
```
## 검증 패턴
 
### 입력 검증
```java
// ✅ 입력 검증 패턴
@PostMapping
public ResponseEntity<ApiResponse<CardResponse>> createCard(
        @Valid @RequestBody CreateCardRequest request) {
    // @Valid로 DTO 검증
}
 
@GetMapping('/{cardNumber}')
public ResponseEntity<ApiResponse<CardResponse>> getCard(
        @PathVariable @Pattern(regexp = '^[0-9]{16}$') String cardNumber) {
    // 정규식 패턴 검증
}
 
@PutMapping('/{id}')
public ResponseEntity<ApiResponse<CardResponse>> updateCard(
        @PathVariable @NotNull Long id,
        @Valid @RequestBody UpdateCardRequest request) {
    // 필수 값 검증
}
```
 
### 커스텀 검증
```java
// ✅ 커스텀 검증 패턴
@PostMapping('/transfer')
public ResponseEntity<ApiResponse<TransferResponse>> transfer(
        @Valid @RequestBody TransferRequest request) {
     
    // 비즈니스 로직 검증
    if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidAmountException('이체 금액은 0보다 커야 합니다');
    }
     
    TransferResponse response = cardService.transfer(request);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```
## 회원 정보 획득
### jwt 토큰 기반 로그인 회원 정보 획득
```java
// ✅ jwt 토큰 기반 Member(회원) 정보 획득 Controller 패턴
@RestController
@RequestMapping('/api/v1/cards')
@Validated
@Slf4j
@RequiredArgsConstructor
public class CardSearchController {
     
    private final CardService cardService;
     
    @GetMapping('/search')
    public ResponseEntity<ApiResponse<PageResponse<CardResponse>>> searchCards(
            @Valid CardSearchRequest request,
            @PageableDefault(sort = 'createdAt', direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal(expression = 'username') String membId) {
         
        log.info('요청 memb_id: {}', membId);
         
        Page<Card> cardPage = cardService.searchCards(request, pageable, membId);
        PageResponse<CardResponse> response = cardMapper.toPageResponse(cardPage);
         
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

## 체크리스트
 
### Controller 구조
- [ ] 단일 책임 원칙 준수
- [ ] RESTful 설계 원칙 적용
- [ ] 적절한 HTTP 메서드 사용
- [ ] 일관된 URL 패턴 사용
 
### 어노테이션 사용
- [ ] 필수 어노테이션 모두 적용
- [ ] 적절한 HTTP 상태 코드 반환
- [ ] 검증 어노테이션 적용
- [ ] Swagger 문서화 어노테이션 적용
 
### 검증 및 예외 처리
- [ ] 입력 데이터 검증 구현
- [ ] 비즈니스 로직 검증 구현
- [ ] 일관된 예외 처리
- [ ] 적절한 에러 응답 형식
 
### 성능 및 유지보수
- [ ] Service 계층 위임
- [ ] DTO 변환 로직 분리
- [ ] 코드 중복 제거
- [ ] 테스트 가능한 구조