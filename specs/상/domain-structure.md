#domain-structure.md
## 개요



이 문서는 프로젝트의 도메인 정의, 백엔드 패키지 구조, cross-domain access 원칙, 그리고 mapper 생성 규칙을 정의합니다. 

**데이터 계층의 엄격한 분리**와 **Service 계층의 유연한 접근**을 통해 도메인 경계를 명확히 하면서도 개발 효율성을 보장합니다.




## 1. 프로젝트 도메인 정의



### 1.1 도메인 목록 및 약어



| 업무구분 | Domain Name | 약어 | 설명 | 테이블 prefix |

|---|----|---|---|---|

민감정보는 삭제하였습니다.

| Report | report | RP | 리포트 | tbrp* |

| Monitoring | monitoring | MN | 모니터링 | tbmn* |



### 1.2 도메인 분류



#### 핵심 비즈니스 도메인

- **XXXXX**: XXXXXX



#### 운영 지원 도메인

- **XXXXX**: XXXXXX




#### 시스템 관리 도메인

- **XXXXX**: XXXXXX




#### 엔진 및 분석 도메인

- **XXXXX**: XXXXXX





## 2. 백엔드 패키지 구조



### 2.1 Java 기본 패키지 구조



```

com.poc.backend/

├── common/                    # 공통 모듈

│   ├── config/               # 공통 설정

│   ├── exception/            # 공통 예외

│   ├── util/                 # 공통 유틸리티

│   └── security/             # 보안 관련

├── domain/                   # 도메인별 패키지

│   ├── admin/               # AD - 시스템 관리

│   │   ├── controller/

│   │   ├── service/

│   │   ├── mapper/

│   │   ├── entity/

│   │   └── dto/

민감한 고객사 업무구조 정보는 삭제하였습니다.

│   └── monitoring/          # MN - 모니터링

│       ├── controller/

│       ├── service/

│       ├── mapper/

│       ├── entity/

│       └── dto/

└── Application.java         # 메인 애플리케이션 클래스

```



### 2.2 도메인별 세부 구조



#### 2.2.1 Controller 계층

```java

// ✅ 도메인별 Controller 네이밍 패턴

com.poc.backend.domain.{domain}.controller/

├── {Domain}Controller.java          # 기본 CRUD API

├── {Domain}SearchController.java    # 검색 전용 API

├── {Domain}AdminController.java     # 관리자 API

└── {Domain}InternalController.java  # 내부 시스템 API

```



#### 2.2.2 Service 계층

```java

// ✅ 도메인별 Service 네이밍 패턴

com.poc.backend.domain.{domain}.service/

├── {Domain}Service.java             # 기본 비즈니스 로직

├── {Domain}SearchService.java       # 검색 비즈니스 로직

├── {Domain}ValidationService.java   # 검증 로직

├── {Domain}ExternalService.java     # 외부 연동 로직

└── impl/                           # 구현체

    ├── {Domain}ServiceImpl.java

    ├── {Domain}SearchServiceImpl.java

    └── {Domain}ValidationServiceImpl.java

```



#### 2.2.3 Mapper 계층

```java

// ✅ 도메인별 Mapper 네이밍 패턴

com.poc.backend.domain.{domain}.mapper/

├── {Table}Mapper.java               # 테이블별 기본 Mapper

├── {Table}SearchMapper.java         # 테이블별 검색 Mapper

├── {Table}BatchMapper.java          # 테이블별 배치 Mapper

└── {Table}StatisticsMapper.java     # 테이블별 통계 Mapper

```



#### 2.2.4 Entity 계층

```java

// ✅ 도메인별 Entity 네이밍 패턴

com.poc.backend.domain.{domain}/

├── entity/                         # 엔티티 (테이블 매핑)

│   ├── {Table}.java

│   └── {Table}History.java

├── dto/                            # 데이터 전송 객체

│   ├── request/

│   │   ├── {Domain}CreateRequest.java

│   │   ├── {Domain}UpdateRequest.java

│   │   └── {Domain}SearchRequest.java

│   └── response/

│       ├── {Domain}Response.java

│       ├── {Domain}ListResponse.java

│       └── {Domain}DetailResponse.java

├── enums/                          # 열거형

│   ├── {Domain}Status.java

│   └── {Domain}Type.java

└── vo/                             # 값 객체

    ├── {Domain}Statistics.java

    └── {Domain}Summary.java

```




## 3. Cross-Domain Access 원칙



### 3.1 기본 원칙



#### 3.1.1 도메인 간 직접 접근 금지

```java

// ❌ 금지: 다른 도메인의 Mapper 직접 호출

@Service

public class CustomerServiceImpl {

    @Autowired

    private PaymentMapper paymentMapper; // 금지

    

    @Autowired

    private AccountMapper accountMapper; // 금지

}



// ✅ 권장: 도메인 Service를 통한 접근

@Service

public class CustomerServiceImpl {

    @Autowired

    private PaymentService paymentService; // 권장

    

    @Autowired

    private AccountService accountService; // 권장

}

```



#### 3.1.2 공통 데이터 접근 패턴

```java

// ✅ 공통 서비스를 통한 데이터 접근

@Service

public class CommonDataService {

    

    // 공통 코드 조회

    public List<CommonCode> getCommonCodes(String codeGroup) {

        return commonCodeMapper.findByCodeGroup(codeGroup);

    }

    

    // 사용자 기본 정보 조회

    public UserInfo getUserInfo(Long userId) {

        return userMapper.findBasicInfoById(userId);

    }

}

```



### 3.2 도메인 간 통신 패턴



#### 3.2.1 Service 계층을 통한 통신

```java

민감정보는 삭제하였습니다. 그냥 자바코드예제가 적혀있는 곳입니다.

```



### 3.3 도메인 의존성 관리



#### 3.3.1 허용되는 의존성

```java

// ✅ 허용되는 도메인 간 의존성

민감정보는 삭제하였습니다.
그냥 도메인간 어디서 어디호출은 되고 안되고를 주석으로 표현한 예제였습니다.

```



#### 3.3.2 금지되는 의존성

```java

// ❌ 금지되는 도메인 간 의존성

민감정보는 삭제하였습니다.
그냥 도메인간 어디서 어디호출은 되고 안되고를 주석으로 표현한 예제였습니다.


```



### 3.4 Mapper 사용 정책 강화 (필수)



#### 3.4.1 Cross-domain Mapper 직접 접근 금지

```java

민감정보는 삭제하였습니다.

```



#### 3.4.2 배치·성능 사유의 예외 허용 기준

- 동일 도메인 내, 읽기 전용 조회 최적화 용 `QueryMapper`에 한해 서비스 내부에서 직접 호출 가능

- 반드시: 서비스가 호출하고, 컨트롤러/타 도메인에서 해당 Mapper에 직접 접근 금지

- 반드시: 주석에 사유(성능/배치), 범위(읽기 전용), 대안(캐시/뷰) 명시



#### 3.4.3 테스트 용이성 및 변경 내성

- Mapper는 DB 스키마 변경에 민감하므로, 외부 도메인에서는 Service를 경유해야 변경 영향 격리 가능

- Service 인터페이스를 통해 계약을 고정하고, 내부 구현(쿼리/인덱스/캐시)은 자유롭게 변경



### 3.5 Cross-Domain Facade/Port 패턴

#### 3.5.1 읽기 전용 파사드 도입

```java

민감정보는 삭제하였습니다.

```



### 3.6 구현 체크리스트

- [ ] 타 도메인 Mapper 직접 의존이 없는가?

- [ ] 타 도메인 접근은 Service/ServiceImpl로 추상화되어 있는가?

- [ ] 컨트롤러에서 Mapper 직접 접근을 하지 않는가?

- [ ] 트랜잭션 경계는 도메인 작업 단위로 적절히 설정되었는가?

- [ ] 배치/성능 예외는 주석과 테스트로 근거가 명확한가?



### 3.7 내부 의존성 vs 외부 의존성

- 내부 의존성(Internal Dependency): 동일 코드베이스/런타임 내 다른 도메인의 기능을 Service/ServiceImpl를 통해 호출

  - 예: 민감정보라 삭제하였습니다.

- 외부 의존성(External Dependency): 타 시스템과 네트워크 호출(REST, gRPC, MQ 등)

  - 예: 민감정보라 삭제하였습니다.



권장 규칙

- 내부 의존성: 반드시 타 도메인의 Service/ServiceImpl를 통해 접근. Mapper 직접 접근 금지

- 외부 의존성: 각 도메인의 `ExternalService` 또는 `client`/`adapter` 계층을 통해 캡슐화



### 3.8 Cross-Domain 쓰기 정책(중요)

- 타 도메인이 소유한 테이블에 쓰기(INSERT/UPDATE/DELETE)가 필요하면 반드시 그 도메인의 Service/ServiceImpl를 호출한다

- 쓰기 순서가 중요한 경우, 장애 복구 전략(재시도/보상/사간일관성)을 명시한다



### 3.9 Port/Facade 네이밍 · 패키징 가이드

- 읽기 전용 포트: `{Domain}ReadonlyService`, 구현체: `{Domain}ReadonlyServiceImpl`

- 쓰기/도메인 행위 포트: `{Domain}CommandService`, 구현체: `{Domain}CommandServiceImpl`

- 패키지 권장: `com.poc.backend.domain.{domain}.service` 또는 `service.impl`

- 호출 측(Service)은 Port 인터페이스만 의존하고, 구현체 바인딩은 스프링 빈으로 주입




## 4. Mapper 생성 원칙



### 4.1 도메인별 Mapper 패키지 구조



#### 4.1.1 기본 패키지 구조

```

src/main/java/com/example/app/domain/{domain}/mapper/

├── {Table}Mapper.java               # 기본 CRUD Mapper

├── {Table}SearchMapper.java         # 검색 전용 Mapper

├── {Table}BatchMapper.java          # 배치 작업 Mapper

└── {Table}StatisticsMapper.java     # 통계 Mapper



src/main/resources/mapper/{domain}/

├── {Table}Mapper.xml

├── {Table}SearchMapper.xml

├── {Table}BatchMapper.xml

└── {Table}StatisticsMapper.xml

```



#### 4.1.2 도메인별 Mapper 예시



민감정보 삭제하였습니다.



### 4.2 테이블별 Mapper 분리 원칙



#### 4.2.1 One-to-One 매핑 원칙

```java

민감 정보 삭제하였습니다.

```



#### 4.2.2 Mapper 책임 분리

```java

민감정보 삭제하였습니다.

```



### 4.3 Mapper XML 파일 구조



#### 4.3.1 XML 파일 위치 및 네이밍

```

src/main/resources/mapper/

├── admin/

│   ├── AdminUserMapper.xml

│   └── AdminRoleMapper.xml

...

```



#### 4.3.2 XML 파일 기본 구조

```xml

<!-- ✅ 도메인별 XML Mapper 구조 -->

<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 

    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.poc.backend.domain.{domain}.mapper.{Table}Mapper">

    

    <!-- Result Map 정의 -->

    <resultMap id="{table}ResultMap" type="com.poc.backend.domain.{domain}.entity.{Table}">

        <id property="id" column="id"/>

        <!-- 필드 매핑 -->

    </resultMap>

    

    <!-- 공통 SQL 조각 -->

    <sql id="selectColumns">

        id, column1, column2, created_at, updated_at

    </sql>

    

    <sql id="whereConditions">

        <where>

            <if test="id != null">AND id = #{id}</if>

            <if test="status != null">AND status = #{status}</if>

        </where>

    </sql>

    

    <!-- 기본 CRUD 쿼리 -->

    <select id="findById" resultMap="{table}ResultMap">

        SELECT <include refid="selectColumns"/>

        FROM {table_name}

        WHERE id = #{id}

    </select>

    

    <!-- 나머지 쿼리들... -->

    

</mapper>

```




## 5. 구현 예시



민감정보라 삭제하였습니다.




## 6. 체크리스트



### 6.1 도메인 구조 체크리스트

- [ ] 정의된 14개 도메인에 따른 패키지 구조 준수

- [ ] 도메인별 약어 (AD, AC, FM 등) 일관성 있게 사용

- [ ] 도메인 경계 명확히 정의

- [ ] 도메인별 책임 분리 적절히 구현



### 6.2 패키지 구조 체크리스트

- [ ] com.poc.backend.domain.{domain} 패키지 구조 준수

- [ ] controller, service, mapper, entity, dto 패키지 분리

- [ ] common 패키지를 통한 공통 기능 관리

- [ ] 도메인별 세부 패키지 구조 일관성 유지



### 6.3 Cross-Domain Access 체크리스트

- [ ] 다른 도메인 Mapper 직접 접근 금지

- [ ] Service 계층을 통한 도메인 간 통신

- [ ] 순환 의존성 방지

- [ ] 허용된 의존성 관계만 사용

- [ ] 이벤트 기반 통신 활용 고려



### 6.4 Mapper 생성 체크리스트

- [ ] 테이블별 별도 Mapper 인터페이스 생성

- [ ] 도메인별 mapper 패키지 구조 준수

- [ ] 책임별 Mapper 분리 (기본, 검색, 배치, 통계)

- [ ] XML 파일과 인터페이스 네이밍 일관성

- [ ] One-to-One 테이블-Mapper 매핑 원칙 준수



### 6.5 구현 품질 체크리스트

- [ ] 명확한 네이밍 컨벤션 적용

- [ ] 적절한 어노테이션 사용

- [ ] 예외 처리 및 검증 로직 포함

- [ ] 트랜잭션 경계 적절히 설정

- [ ] 성능 고려사항 반영 (배치, 페이징 등)

- [ ] 정의된 14개 도메인에 따른 패키지 구조 준수

- [ ] 도메인별 약어 (AD, AC, FM 등) 일관성 있게 사용

- [ ] 도메인 경계 명확히 정의

- [ ] 도메인별 책임 분리 적절히 구현



### 6.2 패키지 구조 체크리스트

- [ ] com.poc.backend.domain.{domain} 패키지 구조 준수

- [ ] controller, service, mapper, entity, dto 패키지 분리

- [ ] common 패키지를 통한 공통 기능 관리

- [ ] 도메인별 세부 패키지 구조 일관성 유지



### 6.3 Cross-Domain Access 체크리스트

- [ ] 다른 도메인 Mapper 직접 접근 금지

- [ ] Service 계층을 통한 도메인 간 통신

- [ ] 순환 의존성 방지

- [ ] 허용된 의존성 관계만 사용

- [ ] 이벤트 기반 통신 활용 고려



### 6.4 Mapper 생성 체크리스트

- [ ] 테이블별 별도 Mapper 인터페이스 생성

- [ ] 도메인별 mapper 패키지 구조 준수

- [ ] 책임별 Mapper 분리 (기본, 검색, 배치, 통계)

- [ ] XML 파일과 인터페이스 네이밍 일관성

- [ ] One-to-One 테이블-Mapper 매핑 원칙 준수



### 6.5 구현 품질 체크리스트

- [ ] 명확한 네이밍 컨벤션 적용

- [ ] 적절한 어노테이션 사용

- [ ] 예외 처리 및 검증 로직 포함

- [ ] 트랜잭션 경계 적절히 설정

- [ ] 성능 고려사항 반영 (배치, 페이징 등)