# Step 1 – 순수 예약 코어 (DB Only)

> **목표 요약**  
> Step 1에서는 **MySQL + 트랜잭션만으로 동작하는 예약 코어**를 만든다.  
> 기능은 `Slot / Reservation / Idempotency`에 한정하고,  
> **동시성 제어 + 멱등성 + 기본 부하 테스트(k6)**까지 포함한다.

---

## 1. Step 1에서 구현할 기능

### 1.1 도메인 기능

#### 1) Slot (예약 가능한 자원/시간 단위)

**기능**

- Slot 생성
    - `title`, `startAt`, `endAt`, `capacity`를 받아 슬롯 생성
    - `remaining = capacity`, `status = OPEN` 으로 초기화
- Slot 단건 조회
- Slot 리스트 조회 (간단한 필터/페이징 정도)

**핵심 제약**

- `0 <= remaining <= capacity`
- `status == OPEN && remaining > 0` 인 경우에만 예약 허용

---

#### 2) Reservation (예약)

**기능**

- 예약 생성
    - `slotId`, `userId`를 받아 해당 슬롯에 대한 예약 생성
    - Slot의 `remaining` 1 감소
    - `Reservation.status = CONFIRMED`
- 예약 취소
    - `reservationId` (그리고 필요 시 `userId`)를 받아 예약 취소
    - `Reservation.status = CANCELLED`
    - 해당 Slot의 `remaining` 1 증가

**핵심 제약**

- Slot이 `OPEN` + `remaining > 0` 일 때만 예약 생성 가능
- Reservation 상태 전이:
    - `CONFIRMED -> CANCELLED`만 허용
    - `CANCELLED -> CONFIRMED` 불가 (단방향)

---

#### 3) Idempotency (멱등성)

**기능**

- 클라이언트가 `Idempotency-Key` 헤더를 보내면,
    - 같은 키에 대해 **비즈니스 로직 수행은 한 번만** 하고
    - 그 이후 동일 키 요청에는 **저장된 응답을 반환**한다.
- Step 1에서는 **예약 생성 API**에만 적용

**동작 흐름**

1. `Idempotency-Key` 헤더 추출
2. `IdempotencyKey` 테이블에서 key 조회
    - **이미 COMPLETED** → 저장된 응답 반환 (비즈니스 로직 실행 X)
    - 없으면:
        - 새 row 저장(PENDING)
        - 비즈니스 로직 실행 (예약 생성)
        - 응답 JSON 저장 + status=COMPLETED

---

### 1.2 API 기능

#### 1) Slot API

- `POST /api/slots`
    - Slot 생성
- `GET /api/slots/{slotId}`
    - 단건 조회
- `GET /api/slots`
    - 리스트 조회 (간단 페이징/필터)

---

#### 2) Reservation API

- `POST /api/reservations`
    - 헤더: `Idempotency-Key: <uuid>`
    - 바디: `{ "slotId": 1, "userId": 123 }`
    - 기능:
        - Slot/Reservation/멱등성 포함한 예약 생성 플로우 처리
- `POST /api/reservations/{id}/cancel`
    - 예약 취소
    - (선택) 멱등성 적용 가능

---

## 2. Step 1에서 사용할 기술 스택

### 2.1 서버 & 프레임워크

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Web (spring-boot-starter-web)**
    - REST API 구현
- **Spring Data JPA (spring-boot-starter-data-jpa)**
    - MySQL과의 ORM 매핑, 트랜잭션 처리

---

### 2.2 데이터베이스

- **MySQL (InnoDB)**
    - 트랜잭션 / 행 단위 락 / 외래키로 정합성 보장
- (로컬 개발용) Docker 기반 MySQL 또는 내장형 DB (테스트에서만)

---

### 2.3 도구 및 공통 라이브러리

- **Lombok** (선택)
    - 엔티티/DTO 보일러플레이트 감소
- **MapStruct 또는 직접 매핑**
    - 엔티티 ↔ DTO 변환

---

### 2.4 테스트 관련 기술

- **JUnit 5**
    - 단위/통합 테스트 프레임워크
- **Spring Boot Test**
    - `@SpringBootTest`, `@DataJpaTest` 등
- **Testcontainers**
    - 실제 MySQL 컨테이너를 띄워 통합 테스트 수행
    - DB 동작을 실제와 최대한 유사하게 검증

---

### 2.5 부하 테스트 (Step 1 범위)

- **k6**
    - HTTP 레벨에서 `POST /api/reservations`를 다중 사용자/다중 요청으로 두드려보는 **간단한 부하 테스트**
    - 목적:
        - 동시성 이슈(초과예약, deadlock 등)가 없는지 기본 검증
        - 대략적인 응답 시간/실패율 확인

> 주의: Step 1의 k6 부하 테스트는 **“DB 코어만 두드려보는 미니 부하 테스트”** 수준이다.  
> 전체 E2E(Queue + SSE + Outbox + Worker)는 Step 5에서 본격적으로 테스트한다.

---

## 3. Step 1 테스트 코드 – 무엇을 검증해야 하는가?

### 3.1 테스트 레이어 구성

1. **도메인 단위 테스트**
    - 엔티티/도메인 로직 검증
2. **서비스 단위 테스트**
    - ReservationService / IdempotencyService 의 순수 비즈니스 로직
3. **통합 테스트**
    - 실제 MySQL(Testcontainers) + JPA 동작 검증
4. **동시성 테스트**
    - 여러 스레드에서 동시에 예약 넣었을 때 초과예약 방지 확인
5. **API 테스트**
    - Controller 레벨에서 멱등성 / 에러 응답 등 검증

---

### 3.2 도메인 단위 테스트 항목

#### 1) `SlotTest`

- `remaining` 감소/증가 로직 검증
- capacity/remaining 제약 검증

예시 시나리오:

- `capacity=10, remaining=10`
    - `decreaseRemaining()` 1회 → `remaining=9`
    - `increaseRemaining()` 1회 → `remaining=10`
- `remaining=0` 상태에서 `decreaseRemaining()` 호출 시 예외 발생

---

#### 2) `ReservationTest`

- 상태 전이 규칙 검증
    - `CONFIRMED -> CANCELLED`는 허용
    - `CANCELLED -> CONFIRMED` 시도 시 예외

---

### 3.3 서비스 단위 테스트 항목

#### 1) `ReservationServiceTest`

- **정상 예약 생성**
    - given: `Slot(remaining=10)`
    - when: 예약 1건 생성
    - then:
        - Reservation 1건 생성
        - Slot.remaining == 9

- **SOLD_OUT 처리**
    - given: `Slot(remaining=0)`
    - when: 예약 생성 시도
    - then:
        - `SoldOutException` 발생 (예상 예외 타입)

- **예약 취소**
    - given: CONFIRMED 상태 예약, Slot.remaining=5
    - when: 취소
    - then:
        - Reservation.status == CANCELLED
        - Slot.remaining == 6

---

#### 2) `IdempotencyServiceTest`

- **새 키**
    - given: 저장된 IdempotencyKey 없음
    - when: `executeWithIdempotency("key-1", ...)` 실행
    - then:
        - 비즈니스 로직 실제 1회 실행
        - IdempotencyKey row 생성 + COMPLETED
        - 저장된 응답 JSON과 반환 응답이 동일

- **기존 COMPLETED 키**
    - given: `key-1`로 COMPLETED 상태 row 존재
    - when: 다시 `executeWithIdempotency("key-1", ...)` 호출
    - then:
        - 비즈니스 로직 실행되지 않음 (Mock/Spy로 호출 횟수 검증)
        - 저장된 응답 그대로 반환

---

### 3.4 통합 테스트 (JPA + MySQL)

#### 1) `ReservationServiceIntegrationTest`

- 실제 MySQL(Testcontainers) 환경에서:

    - Slot + Reservation 저장/조회가 기대대로 동작하는지
    - 트랜잭션 롤백/커밋 시점이 맞는지

시나리오 예시:

1. Slot 1개 생성 (`capacity=5`)
2. Reservation 3개 생성
3. Slot.remaining == 2, Reservation count == 3 확인
4. 하나 취소 → remaining == 3

---

### 3.5 동시성 테스트 (핵심)

#### 1) `ReservationConcurrencyTest` (이름은 자유)

목표:

- **capacity=10인 Slot에 대해 100개 이상의 동시 요청을 날려도 정확히 10개만 예약되는지** 확인

예시 (개념):

- given:
    - `Slot(capacity=10, remaining=10)`
- when:
    - 50~100개의 스레드가 동시에 `createReservation(slotId, userId_i, idemKey_i)` 호출
- then:
    - `ReservationRepository.countBySlotId(slotId) == 10`
    - Slot.remaining == 0

이 테스트는 Step 1의 **품질 게이트** 역할을 한다.  
이게 깨진 상태에서는 Step 2(대기열)로 넘어가면 안 된다.

---

### 3.6 API 테스트

#### 1) `ReservationControllerTest`

- **정상 예약**
    - `POST /api/reservations` → 200 OK, body 필드 검증
- **멱등성**
    - 같은 `Idempotency-Key`로 2번 호출:
        - 1번째 응답: 200 OK, 실제 예약 생성
        - 2번째 응답: 200 OK, body 동일, DB 상태 변화 없음
- **SOLD_OUT 케이스**
    - remaining=0인 Slot에 대해 요청 → 409 Conflict + 에러 코드/메시지 검증

---

## 4. Step 1 – k6 부하 테스트 시나리오

> Step 1에서는 **“예약 코어(예약 API)”만 대상으로 하는 간단한 k6 부하 테스트**를 수행한다.  
> 전체 플로우(Queue/SSE/Outbox)는 아직 없으므로,  
> 주로 `POST /api/reservations`에 집중한다.

### 4.1 k6 테스트 목표

1. **초과예약이 발생하지 않는지** (정합성 관점)
2. 간단한 부하(예: 초당 수십~수백 요청)에서:
    - 에러율이 없는지
    - 응답 시간(p95 정도)이 지나치게 크지 않은지

> 정밀한 성능 튜닝/병목 분석은 Step 5에서 다시 수행한다.

---

### 4.2 사전 준비

- 서버 실행 (예: `http://localhost:8080`)
- 테스트용 Slot 1개 생성 (capacity=100 등)
    - 수동으로 API 호출해서 생성하거나
    - k6 `setup()` 함수에서 한 번 생성해도 됨

---

### 4.3 k6 스크립트 예시 – 동시 예약 부하

`loadtest/step1-reservation-basic.js` (예시 이름)

```js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomSeed, uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

randomSeed(1234);

// 사전에 만들어 둔 슬롯 ID (수동 생성 or setup에서 생성)
const SLOT_ID = 1;

export const options = {
  vus: 50,           // 동시 사용자 수
  duration: '30s',   // 30초 동안 지속
};

export default function () {
  const userId = Math.floor(Math.random() * 1000000);
  const idemKey = uuidv4();

  const url = 'http://localhost:8080/api/reservations';
  const payload = JSON.stringify({
    slotId: SLOT_ID,
    userId: userId,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': idemKey,
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200 or 409': (r) => r.status === 200 || r.status === 409,
  });

  sleep(Math.random() * 0.2);
}
