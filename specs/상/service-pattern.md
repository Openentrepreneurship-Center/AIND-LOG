#service-pattern.md
## 개요
 
이 규칙은 Spring Boot Service 클래스 작성을 위한 표준 패턴을 정의합니다. 비즈니스 로직을 담당하는 Service 계층의 설계 원칙과 구현 패턴을 제공합니다.
 
## Service 설계 원칙
 
### 1. 비즈니스 로직 중심
- 비즈니스 규칙과 로직을 Service 계층에 집중
- 트랜잭션 관리
- 도메인 객체 간의 협력 조정
 
### 2. 단일 책임 원칙
- 하나의 Service는 하나의 도메인 또는 기능만 담당
- 복잡한 비즈니스 로직은 여러 Service로 분할
 
### 3. 의존성 주입
- 생성자 주입을 통한 의존성 관리
- 인터페이스를 통한 느슨한 결합
 
## Service 패턴 구조
 
### 템플릿 기반 구현
이 패턴은 `templates/springboot/service-template.java` 템플릿을 기반으로 구현됩니다.
 
#### 템플릿 사용 방법
1. 템플릿 파일을 복사하여 새로운 Service 생성
2. `{Resource}` 플레이스홀더를 실제 리소스명으로 교체
3. `{resource}` 플레이스홀더를 소문자 리소스명으로 교체
4. 필요한 메서드만 선택적으로 구현
 
#### 기본 Service 구조
```java
// ✅ 템플릿 기반 Service 패턴
// templates/springboot/service-template.java 참조
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class {Resource}ServiceImpl implements {Resource}Service {
     
    private final {Resource}Repository {resource}Repository;
    private final {Resource}Mapper {resource}Mapper;
    private final ValidationService validationService;
    private final NotificationService notificationService;
     
    // 기본 CRUD 메서드들
    // - findById(Long id)
    // - findAll()
    // - findByCondition({Resource}SearchRequest request, Pageable pageable)
    // - create(Create{Resource}Request request)
    // - update(Long id, Update{Resource}Request request)
    // - delete(Long id)
     
    // 비즈니스 로직 메서드들
    // - activate(Long id)
    // - deactivate(Long id)
    // - search{Resource}s({Resource}SearchRequest request)
}
```
 
### 특화된 Service 패턴
 
#### 복잡한 비즈니스 로직 Service
```java
// ✅ 복잡한 비즈니스 로직 Service 패턴
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CardTransactionService {
     
    private final CardService cardService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
     
    @Transactional
    public TransferResult transfer(TransferRequest request) {
        log.info("이체 요청 시작: fromCard={}, toCard={}, amount={}",
                maskCardNumber(request.getFromCardNumber()),
                maskCardNumber(request.getToCardNumber()),
                request.getAmount());
         
        try {
            // 1. 카드 검증
            Card fromCard = validateAndGetCard(request.getFromCardNumber());
            Card toCard = validateAndGetCard(request.getToCardNumber());
             
            // 2. 잔액 검증
            validateBalance(fromCard, request.getAmount());
             
            // 3. 이체 실행
            TransferResult result = executeTransfer(fromCard, toCard, request);
             
            // 4. 거래 기록
            Transaction transaction = recordTransaction(request, result);
             
            // 5. 알림 발송
            sendNotifications(fromCard, toCard, result);
             
            // 6. 감사 로그
            auditService.logTransfer(transaction);
             
            log.info("이체 완료: transactionId={}", transaction.getId());
             
            return result;
             
        } catch (Exception e) {
            log.error("이체 실패: request={}", request, e);
            throw new TransferException("이체 처리 중 오류가 발생했습니다", e);
        }
    }
     
    private Card validateAndGetCard(String cardNumber) {
        Card card = cardService.findByCardNumber(cardNumber);
         
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("카드가 활성 상태가 아닙니다: " + cardNumber);
        }
         
        return card;
    }
     
    private void validateBalance(Card card, BigDecimal amount) {
        if (card.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("잔액이 부족합니다");
        }
    }
     
    @Transactional
    protected TransferResult executeTransfer(Card fromCard, Card toCard, TransferRequest request) {
        // 출금
        fromCard.withdraw(request.getAmount());
        cardService.update(fromCard);
         
        // 입금
        toCard.deposit(request.getAmount());
        cardService.update(toCard);
         
        return TransferResult.builder()
                .success(true)
                .transactionId(generateTransactionId())
                .amount(request.getAmount())
                .build();
    }
     
    private Transaction recordTransaction(TransferRequest request, TransferResult result) {
        Transaction transaction = Transaction.builder()
                .fromCardNumber(request.getFromCardNumber())
                .toCardNumber(request.getToCardNumber())
                .amount(request.getAmount())
                .transactionId(result.getTransactionId())
                .status(TransactionStatus.COMPLETED)
                .build();
         
        return transactionRepository.save(transaction);
    }
     
    private void sendNotifications(Card fromCard, Card toCard, TransferResult result) {
        notificationService.sendTransferNotification(fromCard, result);
        notificationService.sendTransferNotification(toCard, result);
    }
}
```
 
#### 검색 및 필터링 Service
```java
// ✅ 검색 Service 패턴
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CardSearchService {
     
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CacheService cacheService;
     
    @Transactional(readOnly = true)
    public Page<Card> searchCards(CardSearchRequest request, Pageable pageable) {
        log.debug("카드 검색 시작: request={}", request);
         
        // 캐시 키 생성
        String cacheKey = generateCacheKey(request, pageable);
         
        // 캐시에서 조회 시도
        Page<Card> cachedResult = cacheService.get(cacheKey);
        if (cachedResult != null) {
            log.debug("캐시에서 검색 결과 조회: cacheKey={}", cacheKey);
            return cachedResult;
        }
         
        // DB에서 검색
        Page<Card> result = cardRepository.searchCards(request, pageable);
         
        // 캐시에 저장
        cacheService.put(cacheKey, result, Duration.ofMinutes(10));
         
        log.debug("카드 검색 완료: totalElements={}", result.getTotalElements());
         
        return result;
    }
     
    @Transactional(readOnly = true)
    public List<Card> findCardsByUser(Long userId) {
        log.debug("사용자별 카드 조회 시작: userId={}", userId);
         
        List<Card> cards = cardRepository.findByUserId(userId);
         
        log.debug("사용자별 카드 조회 완료: userId={}, count={}", userId, cards.size());
         
        return cards;
    }
     
    @Transactional(readOnly = true)
    public List<Card> findCardsByStatus(CardStatus status) {
        log.debug("상태별 카드 조회 시작: status={}", status);
         
        List<Card> cards = cardRepository.findByStatus(status);
         
        log.debug("상태별 카드 조회 완료: status={}, count={}", status, cards.size());
         
        return cards;
    }
     
    private String generateCacheKey(CardSearchRequest request, Pageable pageable) {
        return String.format("card_search:%s:%s:%s:%s",
                request.getCardType(),
                request.getStatus(),
                pageable.getPageNumber(),
                pageable.getPageSize());
    }
}
```
 

## 트랜잭션 관리 패턴
 
### 트랜잭션 어노테이션 사용
```java
// ✅ 트랜잭션 어노테이션 패턴
@Service
@Transactional
public class CardService {
     
    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션
    public Card findById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("카드를 찾을 수 없습니다"));
    }
     
    @Transactional  // 기본 트랜잭션 (읽기/쓰기)
    public Card create(CreateCardRequest request) {
        Card card = cardMapper.toEntity(request);
        return cardRepository.save(card);
    }
     
    @Transactional(rollbackFor = {BusinessException.class})  // 특정 예외 시 롤백
    public void transfer(TransferRequest request) {
        // 이체 로직
    }
     
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // 새로운 트랜잭션
    public void auditLog(AuditEvent event) {
        // 감사 로그 기록
    }
}
```
 
### 트랜잭션 경계 설정
```java
// ✅ 트랜잭션 경계 설정 패턴
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
     
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
     
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 1. 주문 생성
        Order order = createOrderEntity(request);
        order = orderRepository.save(order);
         
        // 2. 결제 처리
        PaymentResult paymentResult = paymentService.processPayment(order);
         
        // 3. 재고 차감
        inventoryService.decreaseStock(order.getItems());
         
        // 4. 알림 발송 (별도 트랜잭션)
        notificationService.sendOrderConfirmation(order);
         
        return order;
    }
     
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOrderConfirmation(Order order) {
        // 알림 발송은 별도 트랜잭션으로 처리
        notificationService.sendEmail(order.getUser().getEmail(), "주문 확인");
    }
}
```
 

## 예외 처리 패턴
 
### 비즈니스 예외 정의
```java
// ✅ 비즈니스 예외 패턴
public class BusinessException extends RuntimeException {
    private final String errorCode;
     
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
     
    public String getErrorCode() {
        return errorCode;
    }
}
 
public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(String message) {
        super(message, "INSUFFICIENT_BALANCE");
    }
}
 
public class InvalidCardStatusException extends BusinessException {
    public InvalidCardStatusException(String message) {
        super(message, "INVALID_CARD_STATUS");
    }
}
```
 
### Service 레벨 예외 처리
```java
// ✅ Service 레벨 예외 처리 패턴
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CardService {
     
    public Card findByCardNumber(String cardNumber) {
        try {
            return cardRepository.findByCardNumber(cardNumber)
                    .orElseThrow(() -> new CardNotFoundException("카드를 찾을 수 없습니다: " + cardNumber));
        } catch (CardNotFoundException e) {
            log.warn("카드를 찾을 수 없습니다: {}", cardNumber);
            throw e;
        } catch (Exception e) {
            log.error("카드 조회 중 오류 발생: {}", cardNumber, e);
            throw new CardServiceException("카드 조회 중 오류가 발생했습니다", e);
        }
    }
     
    public void transfer(TransferRequest request) {
        try {
            // 이체 로직
            executeTransfer(request);
        } catch (InsufficientBalanceException e) {
            log.warn("잔액 부족으로 이체 실패: {}", request);
            throw e;
        } catch (Exception e) {
            log.error("이체 처리 중 오류 발생: {}", request, e);
            throw new TransferException("이체 처리 중 오류가 발생했습니다", e);
        }
    }
}
```
 
## 검증 패턴
 
### 비즈니스 검증
```java
// ✅ 비즈니스 검증 패턴
@Service
@Transactional
@RequiredArgsConstructor
public class CardValidationService {
     
    private final CardRepository cardRepository;
    private final UserService userService;
     
    public void validateCreateCard(CreateCardRequest request) {
        // 사용자 존재 여부 검증
        if (!userService.existsById(request.getUserId())) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + request.getUserId());
        }
         
        // 카드 번호 중복 검증
        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            throw new DuplicateCardNumberException("이미 존재하는 카드 번호입니다: " + request.getCardNumber());
        }
         
        // 카드 타입별 제한 검증
        validateCardTypeLimits(request.getUserId(), request.getCardType());
    }
     
    public void validateTransfer(TransferRequest request) {
        // 카드 존재 여부 검증
        Card fromCard = cardRepository.findByCardNumber(request.getFromCardNumber())
                .orElseThrow(() -> new CardNotFoundException("출금 카드를 찾을 수 없습니다"));
         
        Card toCard = cardRepository.findByCardNumber(request.getToCardNumber())
                .orElseThrow(() -> new CardNotFoundException("입금 카드를 찾을 수 없습니다"));
         
        // 카드 상태 검증
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("출금 카드가 활성 상태가 아닙니다");
        }
         
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("입금 카드가 활성 상태가 아닙니다");
        }
         
        // 잔액 검증
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("잔액이 부족합니다");
        }
         
        // 이체 한도 검증
        validateTransferLimit(fromCard, request.getAmount());
    }
     
    private void validateCardTypeLimits(Long userId, CardType cardType) {
        long cardCount = cardRepository.countByUserIdAndCardType(userId, cardType);
         
        switch (cardType) {
            case BASIC:
                if (cardCount >= 3) {
                    throw new CardLimitExceededException("기본 카드는 최대 3개까지 발급 가능합니다");
                }
                break;
            case PREMIUM:
                if (cardCount >= 1) {
                    throw new CardLimitExceededException("프리미엄 카드는 최대 1개까지 발급 가능합니다");
                }
                break;
        }
    }
     
    private void validateTransferLimit(Card card, BigDecimal amount) {
        BigDecimal dailyLimit = card.getDailyTransferLimit();
        BigDecimal dailyUsed = card.getDailyTransferUsed();
         
        if (dailyUsed.add(amount).compareTo(dailyLimit) > 0) {
            throw new TransferLimitExceededException("일일 이체 한도를 초과했습니다");
        }
    }
}
```
 

## 체크리스트
 
### Service 구조
- [ ] 인터페이스와 구현체 분리
- [ ] 단일 책임 원칙 준수
- [ ] 의존성 주입 사용
- [ ] 적절한 패키지 구조
 
### 비즈니스 로직
- [ ] 비즈니스 규칙 구현
- [ ] 트랜잭션 경계 설정
- [ ] 검증 로직 구현
- [ ] 예외 처리 구현
 
### 트랜잭션 관리
- [ ] 적절한 트랜잭션 어노테이션 사용
- [ ] 읽기 전용 트랜잭션 활용
- [ ] 트랜잭션 전파 설정
- [ ] 롤백 정책 정의
 
### 예외 처리
- [ ] 비즈니스 예외 정의
- [ ] 적절한 예외 계층 구조
- [ ] 로깅과 예외 처리
- [ ] 사용자 친화적 에러 메시지
 
### 성능 및 유지보수
- [ ] 캐싱 전략 적용
- [ ] 로깅 구현
- [ ] 테스트 가능한 구조
- [ ] 코드 중복 제거