#naming_rule.md
## 0. 개요
- 입력 기준: `feature.name` (한글 기능명) 
- 출력 대상: Controller, Service, DTO, URI
- 핵심 원칙
  - `feature.name`에서 `동사(Feature) + 나머지(명사구)` 형태로 파악한다.
  - 주동사는 표준 Feature 세트에서 변환하고, 이후 단어 각각 순서 그대로 명사구 1:1 변환한다.
  - `feature.name`의 각 모든 한글 단어를 표준 영문 용어와 1:1로 매핑하여 영어 명칭을 생성한다.
  - Controller, Service, DTO, URI 에서 동일하게 사용한다.
  - 엔티티명은 테이블명 기준으로 약어를 해석하지 않고 그대로 변환 규칙을 사용한다.

## 1. 변환 규칙
| 단계 | 규칙 | 예시 |
| --- | --- | --- |
| 0. 한글 단어 추출 | 한글 기능명의 각 한글 단어 단위로 분리한다. | `약관동의정보조회` → `약관`, `동의`, `정보`, `조회` |
| 1. 동사(Feature) 추출 | 기능명(주동사)를 Feature 세트에서 1:1 매핑 | “조회” → Retrieve |
| 2. 나머지 단어 변환 | 나머지 단어는 표준 단어 매핑 세트에서 각각 모두 순서 유지하며 1:1 명사구 영단어 매핑 | `약관`, `동의`, `정보` → TermsAgreeInfo (순서유
| 3. 결합 | `{Feature}{나머지명사구}` 형태로 결합 | RetrieveTermsAgreeInfo |
| 4. 활용 | 생성된 명칭은 Controller, Service, DTO, URI 계층에 동일하게 사용 | `RetrieveTermsAgreeInfoResDto`, `retrieveTermsAgreeInfo()`, `/retrieve-terms-agree-info` |


## 2. Feature 표준 세트
| 한글 동사 | Feature | HTTP |
| --- | --- | --- |
| 조회 | Retrieve | GET |
| 등록/생성/발행 | Register | POST |
| 수정 | Update | PUT |
| 삭제 | Delete | DELETE |
| 검색 | Search | GET |
| 활성화 | Activate | PUT |
| 비활성화 | Deactivate | PUT |
| 검증 | Verify | POST |
| 결과/요약/통계 조회 | Retrieve | GET |

> `및`으로 연결된 동사는 생략하고 대표 의미 하나로 표현(주동사)
> 예) `생성및저장` → `register`




## 3. 표준 단어 매핑 세트
| 한글 단어 | 영문 변환 |
| --- | --- |
| 회원 / 사용자 | Member / User |
| 계정 | Account |
| 카드 | Card |
| OTP | Otp |
| 결과 | Result |
| 요약 | Summary |
| 통계 | Statistics |
| 약관 | Terms |
| 동의 | Agree |
| 정보 | Info |
| 내역 | History |
| 서비스체크 | ServiceCheck |
| 2단계 인증 | TwoFactorAuth |

> 제시된 표에 없는 단어는 동일한 의미의 영어 단어로 매핑한다.

> 각 한글 단어 모두 제시된 순서대로 명사구 1:1로 대응한다.


## 4. 계층별 규칙
### 4.1 Controller
| 항목 | 규칙 |
| --- | --- |
| 패키지 | `com.poc.backend.domain.{level1-domain}.controller` |
| 클래스명 | `{level2-domain}Controller.java` |
| 메서드명 | `{featureLower}{명사구}()` |
| 예시 | `retrieveMemberInfo()`, `verifyOTP()` |

### 4.2 Mapper / Entity
> 테이블명 변환 규칙
> * `TB_` 접두 제거
> * 도메인 약어(`_AC`, `_CS`, …) 제거
> * `_M`, `_H`, `_D` 접미 제거
> * 약어는 해석하지 않고 그대로 PascalCase 변환

#### Mapper
| 항목 | 규칙 |
| --- | --- |
| 패키지 | `com.poc.backend.domain.{level1-domain}.mapper` |
| XML 경로 | `src/main/resources/mybatis/mapper/{level1-domain}/` |
| XML 파일명 | `{Table명_변환}Mapper.xml` |
| 인터페이스명 | `{Table명_변환}Mapper.java` |
| 예시 | `TB_CS_AUTN_M` → `AutnMapper.xml`, `AutnMapper.java` |

#### Entity
| 항목 | 규칙 |
| --- | --- |
| 패키지  | `com.poc.backend.domain.{level1-domain}.entity` |
| 클래스명 | `{Table명_변환}Entity.java` |
| 예시   | `TB_CS_AUTN_M` → `AutnEntity.java` |

### 4.3 DTO
| 항목 | 규칙 |
| --- | --- |
| 패키지 | Request: `com.poc.backend.domain.{level1-domain}.dto.request`<br>Response: `com.poc.backend.domain.{level1-domain}.dto.response` |
| 클래스명 | `{Feature}{명사구}{Req or Res}Dto.java` |
| Wrapper Class 예외 | Wrapper Class (Integer, Long, String 등)일 경우 DTO 없음, 그대로 사용 |


## 5. Endpoint URI 규칙
### 패턴
```
/api/v1/{level1-domain}/{level2-domain(kebab-case)}/{feature}-{명사구}
```
- 소문자 + 하이픈(kebab-case) 로 변환
- 예시: `account-management/retrieve-member-info`, `/verify-otp`, `/register-card`


## 6. 형식 일관성 원칙
| 항목 | 규칙 |
| --- | --- |
| 클래스/파일명 | PascalCase (맨 앞 대문자) |
| 메서드명 | camelCase (맨 앞 소문자) |
| URI | kebab-case (소문자-하이픈) |
| 명사구 | 각 한글 단어 순서 그대로 명사구 1:1로 매핑 |
| 명칭 일관성 | Controller, Service, DTO, URI 동일 명칭 적용 |
| 테이블명 → 엔티티명 변환 규칙 | `TB_` 접두 제거 + 도메인 약어(예: `_AC` 등) 제거 + `_M/_H` 접미 제거 후 테이블명 약어를 풀지않고 그대로 PascalCase |