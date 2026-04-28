#mapper-pattern.md
## 개요
 
이 규칙은 Spring Boot에서 MyBatis Mapper 인터페이스 작성을 위한 표준 패턴을 정의합니다. 데이터 접근 계층의 설계 원칙과 구현 패턴을 제공합니다.
 
## Mapper 설계 원칙
 
### 1. 데이터 접근 추상화
- 데이터베이스 접근 로직을 Mapper 계층에 집중
- SQL과 Java 코드의 분리
- 재사용 가능한 쿼리 컴포넌트
 
### 2. 성능 최적화
- 효율적인 SQL 쿼리 작성
- 적절한 인덱스 활용
- 배치 처리 지원
 
### 3. 유지보수성
- 명확한 네이밍 컨벤션
- 일관된 구조
- 문서화된 쿼리
 
## Mapper 인터페이스 패턴
 
### 템플릿 기반 구현
이 패턴은 `templates/springboot/mapper-template.java` 템플릿을 기반으로 구현됩니다.
 
#### 템플릿 사용 방법
1. 템플릿 파일을 복사하여 새로운 Mapper 인터페이스 생성
2. `{Resource}` 플레이스홀더를 실제 리소스명으로 교체
3. `{resource}` 플레이스홀더를 소문자 리소스명으로 교체
4. `{UniqueField}` 플레이스홀더를 고유 필드명으로 교체
5. 필요한 메서드만 선택적으로 구현
 
#### 기본 Mapper 구조
```java
// ✅ 템플릿 기반 Mapper 패턴
// templates/springboot/mapper-template.java 참조
@Mapper
public interface {Resource}Mapper {
     
    // 기본 CRUD 작업
    // - findById(Long id)
    // - findAll()
    // - insert({Resource} {resource})
    // - update({Resource} {resource})
    // - deleteById(Long id)
     
    // 조건부 조회
    // - findByStatus(String status)
    // - findByUserId(Long userId)
    // - findBy{UniqueField}(String {uniqueField})
     
    // 복합 조건 조회
    // - findByCondition({Resource}SearchRequest request)
    // - findByConditionWithPaging({Resource}SearchRequest request, Pageable pageable)
     
    // 검색
    // - search{Resource}s({Resource}SearchRequest request)
    // - search{Resource}sWithPaging({Resource}SearchRequest request, Pageable pageable)
     
    // 배치 작업
    // - batchInsert(List<{Resource}> {resource}s)
    // - batchUpdate(List<{Resource}> {resource}s)
    // - batchDeleteByIds(List<Long> ids)
     
    // 통계 및 집계
    // - countByStatus(String status)
    // - countByUserId(Long userId)
    // - getStatistics()
     
    // 존재 여부 확인
    // - existsById(Long id)
    // - existsBy{UniqueField}(String {uniqueField})
     
    // DTO 변환 메서드들
    // - toResponse({Resource} {resource})
    // - toResponseList(List<{Resource}> {resource}s)
    // - toPageResponse(Page<{Resource}> {resource}Page)
}
```
 
### 특화된 Mapper 인터페이스
 
#### 검색 전용 Mapper
```java
// ✅ 검색 전용 Mapper 패턴
@Mapper
public interface CardSearchMapper {
     
    // 기본 검색
    List<Card> searchCards(CardSearchRequest request);
    Page<Card> searchCardsWithPaging(CardSearchRequest request, Pageable pageable);
     
    // 고급 검색
    List<Card> searchCardsByAdvancedCriteria(AdvancedCardSearchRequest request);
    List<Card> searchCardsByDateRange(LocalDate startDate, LocalDate endDate);
     
    // 통계 검색
    List<CardStatistics> getCardStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
    List<CardStatistics> getCardStatisticsByStatus(CardStatus status);
     
    // 사용자별 검색
    List<Card> findCardsByUserAndStatus(Long userId, CardStatus status);
    List<Card> findCardsByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
```
 
#### 트랜잭션 전용 Mapper
```java
// ✅ 트랜잭션 전용 Mapper 패턴
@Mapper
public interface CardTransactionMapper {
     
    // 잔액 업데이트
    int updateBalance(@Param("cardNumber") String cardNumber,
                     @Param("amount") BigDecimal amount);
     
    // 배치 잔액 업데이트
    int batchUpdateBalance(@Param("updates") List<BalanceUpdateRequest> updates);
     
    // 거래 기록
    int insertTransaction(Transaction transaction);
    int batchInsertTransactions(List<Transaction> transactions);
     
    // 거래 조회
    List<Transaction> findTransactionsByCardNumber(String cardNumber);
    List<Transaction> findTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
     
    // 거래 통계
    List<TransactionStatistics> getTransactionStatistics(String cardNumber);
}
```
 
#### 통계 전용 Mapper
```java
// ✅ 통계 전용 Mapper 패턴
@Mapper
public interface CardStatisticsMapper {
     
    // 기본 통계
    CardStatistics getCardStatistics();
    List<CardStatistics> getCardStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
     
    // 사용자별 통계
    List<UserCardStatistics> getUserCardStatistics();
    UserCardStatistics getUserCardStatistics(Long userId);
     
    // 상태별 통계
    List<StatusCardStatistics> getStatusCardStatistics();
    StatusCardStatistics getStatusCardStatistics(CardStatus status);
     
    // 월별 통계
    List<MonthlyCardStatistics> getMonthlyCardStatistics(int year);
    List<MonthlyCardStatistics> getMonthlyCardStatistics(int year, int month);
     
    // 거래 통계
    List<TransactionStatistics> getTransactionStatistics();
    TransactionStatistics getTransactionStatistics(String cardNumber);
}
```
 
#### 배치 작업 전용 Mapper
```java
// ✅ 배치 작업 전용 Mapper 패턴
@Mapper
public interface CardBatchMapper {
     
    // 대량 삽입
    int batchInsertCards(List<Card> cards);
    int batchInsertCardsWithGeneratedKeys(List<Card> cards);
     
    // 대량 수정
    int batchUpdateCards(List<Card> cards);
    int batchUpdateCardStatuses(@Param("cardIds") List<Long> cardIds,
                               @Param("status") CardStatus status);
     
    // 대량 삭제
    int batchDeleteCards(List<Long> cardIds);
    int batchDeleteCardsByStatus(CardStatus status);
     
    // 대량 조회
    List<Card> batchFindByIds(List<Long> cardIds);
    List<Card> batchFindByCardNumbers(List<String> cardNumbers);
     
    // 대량 존재 여부 확인
    List<Long> findExistingIds(List<Long> cardIds);
    List<String> findExistingCardNumbers(List<String> cardNumbers);
}
```
 
## 어노테이션 사용 패턴
 
### 필수 어노테이션
```java
// ✅ 필수 어노테이션 패턴
@Mapper
public interface CardMapper {
    // Mapper 인터페이스 선언
}
 
// ✅ 파라미터 어노테이션 패턴
@Mapper
public interface CardMapper {
     
    // 단일 파라미터
    Card findByCardNumber(String cardNumber);
     
    // 다중 파라미터
    List<Card> findByUserAndStatus(Long userId, CardStatus status);
     
    // @Param 어노테이션 사용
    int updateBalance(@Param("cardNumber") String cardNumber,
                     @Param("amount") BigDecimal amount);
     
    // 복합 객체 파라미터
    List<Card> findByCondition(CardSearchRequest request);
     
    // 컬렉션 파라미터
    int batchInsert(@Param("cards") List<Card> cards);
    List<Card> findByIds(@Param("ids") List<Long> ids);
}
```
 
### 반환 타입 패턴
```java
// ✅ 반환 타입 패턴
@Mapper
public interface CardMapper {
     
    // 단일 객체
    Card findById(Long id);
    Card findByCardNumber(String cardNumber);
     
    // 컬렉션
    List<Card> findAll();
    List<Card> findByStatus(CardStatus status);
     
    // 페이지네이션
    Page<Card> findByConditionWithPaging(CardSearchRequest request, Pageable pageable);
     
    // 기본 타입
    long countByStatus(CardStatus status);
    boolean existsById(Long id);
    int insert(Card card);
     
    // 통계 객체
    CardStatistics getCardStatistics();
    List<CardStatistics> getCardStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
}
```
 
## 메서드 네이밍 패턴
 
### 기본 CRUD 네이밍
```java
// ✅ 기본 CRUD 네이밍 패턴
@Mapper
public interface CardMapper {
     
    // 조회 (Read)
    Card findById(Long id);
    List<Card> findAll();
    Card findByCardNumber(String cardNumber);
    List<Card> findByStatus(CardStatus status);
     
    // 생성 (Create)
    int insert(Card card);
    int insertCard(Card card);
     
    // 수정 (Update)
    int update(Card card);
    int updateCard(Card card);
    int updateStatus(Long id, CardStatus status);
     
    // 삭제 (Delete)
    int deleteById(Long id);
    int deleteByCardNumber(String cardNumber);
}
```
 
### 조건부 조회 네이밍
```java
// ✅ 조건부 조회 네이밍 패턴
@Mapper
public interface CardMapper {
     
    // 단일 조건
    List<Card> findByUserId(Long userId);
    List<Card> findByCardType(CardType cardType);
    List<Card> findByStatus(CardStatus status);
     
    // 복합 조건
    List<Card> findByUserAndStatus(Long userId, CardStatus status);
    List<Card> findByUserAndCardType(Long userId, CardType cardType);
    List<Card> findByStatusAndCardType(CardStatus status, CardType cardType);
     
    // 범위 조건
    List<Card> findByBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);
    List<Card> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
     
    // 포함 조건
    List<Card> findByCardTypeIn(List<CardType> cardTypes);
    List<Card> findByIdIn(List<Long> ids);
}
```
 
### 검색 및 필터링 네이밍
```java
// ✅ 검색 및 필터링 네이밍 패턴
@Mapper
public interface CardMapper {
     
    // 검색
    List<Card> searchCards(CardSearchRequest request);
    Page<Card> searchCardsWithPaging(CardSearchRequest request, Pageable pageable);
     
    // 필터링
    List<Card> filterByCondition(CardFilterRequest request);
    List<Card> filterByAdvancedCriteria(AdvancedCardFilterRequest request);
     
    // 정렬
    List<Card> findAllOrderByCreatedDateDesc();
    List<Card> findByStatusOrderByBalanceDesc(CardStatus status);
}
```
 
## 체크리스트
 
### Mapper 인터페이스 구조
- [ ] @Mapper 어노테이션 사용
- [ ] 명확한 메서드 네이밍
- [ ] 적절한 파라미터 타입
- [ ] 반환 타입 명시
 
### 메서드 설계
- [ ] 단일 책임 원칙 준수
- [ ] 일관된 네이밍 컨벤션
- [ ] 적절한 파라미터 어노테이션
- [ ] 명확한 반환 타입
 
### 성능 고려사항
- [ ] 배치 작업 메서드 포함
- [ ] 페이징 처리 메서드 포함
- [ ] 통계 및 집계 메서드 포함
- [ ] 존재 여부 확인 메서드 포함
 
### 유지보수성
- [ ] 특화된 Mapper 분리
- [ ] 명확한 메서드 그룹화
- [ ] 일관된 패턴 적용
- [ ] 문서화된 메서드