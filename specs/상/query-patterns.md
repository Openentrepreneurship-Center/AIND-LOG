#query-patterns.md
## 개요

 

이 규칙은 MyBatis 3.x를 사용한 효율적인 쿼리 작성 패턴을 정의합니다.a

성능 최적화, 가독성, 유지보수성을 고려한 쿼리 작성 가이드라인을 제공합니다.

 
## Xml Root 패키지

경로: `\src\main
esources\mybatis\mapper`

하위에 도메인 별로 xml 생성한다.

 


# 기본 쿼리 패턴

 

### 1. 동적 쿼리 작성

```xml

<!-- ✅ 조건부 쿼리 -->

<select id="findCardsByCondition" resultType="Card">

    SELECT id, card_number, balance, status

    FROM cards

    <where>

        <if test="cardType != null">

            AND card_type = #{cardType}

        </if>

        <if test="status != null">

            AND status = #{status}

        </if>

        <if test="minBalance != null">

            AND balance >= #{minBalance}

        </if>

    </where>

    ORDER BY created_at DESC

</select>

```

 

### 2. 결과 매핑

```xml

<!-- ✅ 복잡한 결과 매핑 -->

<resultMap id="cardWithTransactionsMap" type="Card">

    <id property="id" column="card_id"/>

    <result property="cardNumber" column="card_number"/>

    <result property="balance" column="balance"/>

    <result property="status" column="status"/>

    <collection property="transactions" ofType="Transaction">

        <id property="id" column="transaction_id"/>

        <result property="amount" column="amount"/>

        <result property="transactionDate" column="transaction_date"/>

    </collection>

</resultMap>

 

<select id="findCardWithTransactions" resultMap="cardWithTransactionsMap">

    SELECT c.id as card_id, c.card_number, c.balance, c.status,

           t.id as transaction_id, t.amount, t.transaction_date

    FROM cards c

    LEFT JOIN transactions t ON c.id = t.card_id

    WHERE c.card_number = #{cardNumber}

    ORDER BY t.transaction_date DESC

</select>

```

 


## 성능 최적화 패턴

 

### 1. 배치 처리

```xml

<!-- ✅ 배치 삽입 -->

<insert id="batchInsertTransactions" parameterType="java.util.List">

    INSERT INTO transactions (card_id, amount, transaction_date, type)

    VALUES

    <foreach collection="list" item="transaction" separator=",">

        (#{transaction.cardId}, #{transaction.amount},

         #{transaction.transactionDate}, #{transaction.type})

    </foreach>

</insert>

 

<!-- ✅ 배치 업데이트 -->

<update id="batchUpdateCardBalances" parameterType="java.util.List">

    <foreach collection="list" item="card" separator=";">

        UPDATE cards

        SET balance = #{card.balance},

            updated_at = NOW()

        WHERE id = #{card.id}

    </foreach>

</update>

```

 

### 2. 페이징 처리

```xml

<!-- ✅ 페이징 쿼리 -->

<select id="findTransactionsWithPaging" resultType="Transaction">

    SELECT id, card_id, amount, transaction_date, type

    FROM transactions

    WHERE card_id = #{cardId}

    ORDER BY transaction_date DESC

    LIMIT #{limit} OFFSET #{offset}

</select>

```

 


## 동적 SQL 패턴

 

### 1. choose-when-otherwise

```xml

<!-- ✅ 다중 조건 처리 -->

<select id="findCardsByType" resultType="Card">

    SELECT id, card_number, balance, status

    FROM cards

    <choose>

        <when test="cardType == 'STUDENT'">

            WHERE card_type = 'STUDENT' AND user_type = 'STUDENT'

        </when>

        <when test="cardType == 'SENIOR'">

            WHERE card_type = 'SENIOR' AND age >= 65

        </when>

        <otherwise>

            WHERE card_type = #{cardType}

        </otherwise>

    </choose>

</select>

```

 

### 2. foreach 패턴

```xml

<!-- ✅ IN 절 처리 -->

<select id="findCardsByIds" resultType="Card">

    SELECT id, card_number, balance, status

    FROM cards

    WHERE id IN

    <foreach collection="cardIds" item="id" open="(" separator="," close=")">

        #{id}

    </foreach>

</select>

```

 


## 에러 처리 패턴

 

### 1. 예외 처리

```xml

<!-- ✅ 안전한 쿼리 -->

<select id="findCardSafely" resultType="Card">

    SELECT id, card_number, balance, status

    FROM cards

    WHERE card_number = #{cardNumber}

    AND status != 'DELETED'

    LIMIT 1

</select>

```

 
## 공통 코드 Join 패턴

 

### 1. 공통 코드 Join을 위한 CTE 패턴

```xml

<!-- ✅ 다중 공통코드 조인용 CTE 조각 -->

<sql id="multiCommonCodeCTE">
   sql예제는 민감정보가 있었기 때문에 삭제하였습니다.
</sql>

 

<!-- ✅ 회원 정보 조회 (다중 공통코드 조인) -->

<select id="findMemberWithAllCodes" resultType="MemberDto">

    <include refid="multiCommonCodeCTE">

        <property name="codeGroupTable" value="tbadc007"/>

        <property name="codeTable" value="tbadc008"/>

    </include>
    sql예제는 민감정보가 있었기 때문에 삭제하였습니다.
    
</select>

```

 


## 쿼리 최적화 가이드라인

 

### 1. 인덱스 활용

```xml

<!-- ✅ 인덱스를 활용하는 쿼리 -->

<select id="findActiveCardsByUser" resultType="Card">

    SELECT id, card_number, balance

    FROM cards

    WHERE user_id = #{userId}

    AND status = 'ACTIVE'

    ORDER BY created_at DESC

</select>

```

 

### 2. N+1 문제 방지

```xml

<!-- ✅ JOIN을 사용한 N+1 문제 해결 -->

<select id="findCardsWithUserInfo" resultMap="cardWithUserMap">

    SELECT c.id, c.card_number, c.balance,

           u.id as user_id, u.name, u.email

    FROM cards c

    INNER JOIN users u ON c.user_id = u.id

    WHERE c.status = 'ACTIVE'

</select>

```



### 문자열 날짜 비교

```xml

crte_dttm >= TO_TIMESTAMP(#{req.fromCrteDttm}, 'YYYYMMDDHH24MISS')

```

 


## 체크리스트

 

### 쿼리 작성 시

- [ ] 동적 쿼리에 적절한 조건문을 사용했는가?

- [ ] 결과 매핑이 올바르게 설정되었는가?

- [ ] 성능을 고려한 쿼리를 작성했는가?

- [ ] 배치 처리가 필요한 경우 foreach를 사용했는가?

 

### 성능 최적화 시

- [ ] 적절한 인덱스를 활용하는가?

- [ ] N+1 문제를 방지했는가?

- [ ] 페이징 처리를 적용했는가?

- [ ] 불필요한 컬럼을 조회하지 않는가?

 

### 유지보수성

- [ ] 쿼리가 가독성이 좋은가?

- [ ] 재사용 가능한 구조인가?

- [ ] 에러 처리가 적절한가?

- [ ] 주석이 필요한 부분에 추가했는가?

 

## 관련 규칙

 

구체적인 데이터베이스 최적화는 다음 규칙들을 참조하세요:

- @core/performance-guidelines.mdc (성능 가이드라인)

- @frameworks/springboot/api-design.mdc (Spring Boot API 설계)