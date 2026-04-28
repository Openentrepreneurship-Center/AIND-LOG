#exception-handling-pattern.md
## 예외 처리 유형

- **BizException**: 비즈니스 로직 처리 중 예외

- **CustomWebClientException**: 4xx 외부 요청 예외

- **CustomWebServerException**: 5xx 외부 요청 예외


## 예외 처리 전략

- **기본 원칙**: 모든 비즈니스 예외는 `BizException`을 사용하여 일관성 있는 예외 처리

- **예외 계층 구조**: BizException → RuntimeException → Exception

- **예외 전파**: BizException은 그대로 전파, 기타 예외는 적절한 BizErrorCode로 래핑

- **로깅**: 예외 발생 시 에러 코드와 함께 구조화된 로깅 수행




## BizErrorCode

- **경로**: com.poc.backend.domain.common.enums.BizErrorCode.java

- **기본 원칙**: 기존에 등록된 코드를 사용하고, 필요시 추가한다.




## BizErrorCode

- **import 경로**: com.poc.backend.domain.common.enums.BizErrorCode

- **기본 원칙**: 기존에 등록된 코드를 사용하고, 필요시 추가한다.




## 예외 처리 예시

```java

thorws new BizException(BizErrorCode.CARD_NOT_EXIST);

```



```java

thorws new BizException("카드가 존재하지 않습니다.", BizErrorCode.CARD_NOT_EXIST);

```




## 예외 처리 예시

```java

thorws new BizException(BizErrorCode.CARD_NOT_EXIST);

```



```java

thorws new BizException("카드가 존재하지 않습니다.", BizErrorCode.CARD_NOT_EXIST);

```