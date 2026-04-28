#code-generate-guidelines.md
## 적용 범위

* **Java**: Controller, Service, ServiceImpl, Entity, DTO, Mapper 인터페이스

* **MyBatis XML**: Mapper XML (select/insert/update/delete, sql fragment)




## 핵심 원칙

1. **비파괴 편집**: 기존 코드 삭제·리네임·이동 금지. 새 기능은 **신규 파일** 또는 **신규 라인**으로만 추가.

2. **Diff 출력 원칙**: 자동화 도구/LLM은 **Unified Diff**만 출력하고 원문 전체 재생성 금지.

3. **공용 시그니처 고정**: 기존 public 메서드 시그니처/Mapper id는 변경 금지.


### 의존성 처리 규칙

DevSpec 7번 항목의 의존성 상태에 따라 다음과 같이 코드를 생성하세요:

**[EXISTING] 의존성**
1. import 문만 생성
2. 생성자 주입 코드 작성 (@RequiredArgsConstructor 또는 명시적 생성자)
3. 메서드 호출 코드만 작성
4. 실제로 존재하지 않더라도 존재하는 것처럼 여긴다. 주석처리로 처리하지 않는다.
5. ❌ 절대 금지: 클래스 정의, 구현부, 메서드 본문 생성, 주석처리

**[TO_BE_CREATED] 의존성**
1. 클래스 또는 인터페이스 전체 정의 생성
2. 모든 필드 선언
3. 모든 메서드 구현
4. 필요한 어노테이션 모두 포함 (@Entity, @Service, @Component 등)
5. 직접 생성하지는 않고, 주석으로 처리하여 표시한다.

**[INTERFACE] 의존성**
1. import 문 생성
2. 생성자 주입 코드 작성
3. 메서드 호출 시 try-catch 또는 TODO 주석 추가
4. ❌ 절대 금지: 외부 시스템 구현부 생성

### 14번 항목 활용
- "의존성 사용" 섹션을 보고 구체적인 입력값/출력값 확인
- 실제 비즈니스 로직에서 어떤 값을 전달하고 받는지 파악
- 결과 처리 방식을 코드에 반영




## 수정 불가(금지) 규칙

* [ ] 파일 삭제/리네임/이동 금지

* [ ] 기존 public 메서드 시그니처/Mapper `id`/`parameterType`/`resultType` 변경 금지

* [ ] 예외 타입·메시지·트랜잭션 경계 변경 금지

* [ ] XML 네임스페이스/`<mapper namespace>` 변경 금지

* [ ] 기존 테스트의 어설션을 약화시키는 변경 금지