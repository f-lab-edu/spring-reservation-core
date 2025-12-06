# Step 2 – Redis 기반 대기열 (QueueToken / EligibilityToken)

> **한 줄 요약**  
> Step 2에서는 **예약 코어 앞단에 Redis 기반 “대기열(Queue)” 레이어를 추가**한다.  
> 사용자는 먼저 **Queue에 들어가 번호표(QueueToken)를 받고**,  
> 그 중 일부만 **ELIGIBLE 상태로 승급**되어,  
> **EligibilityToken을 들고 Step 1의 예약 코어로 입장**할 수 있게 된다.

---

## 0. Step 2의 역할 – 전체 구조에서 어디까지 담당하나?

### 비유로 이해하기

- Step 1까지:  
  → **티켓 창구(예약 DB)만 있는 상태**  
  → 손님이 몰리면 창구 직원 앞에 난장판처럼 몰린다.

- Step 2부터:  
  → 창구 앞에 **번호표 뽑는 기계 + 전광판(대기열 시스템)**을 설치한다.  
  → 손님은 먼저 번호표(QueueToken)를 받고,
  → 전광판에 “지금 1번~20번까지 입장 가능” 같은 식으로 관리한다.
  → **입장 허가(ELIGIBLE) 받은 사람만 창구(예약 코어)로 보낸다.**

즉, Step 2의 핵심은:

1. **DB 앞단에 Redis Queue를 두고 “정해진 속도만큼” 써주도록 보호**하는 것
2. 예약 요청이 직접 DB로 쏟아지지 않고,  
   **QueueToken / EligibilityToken을 거쳐 제어**되도록 하는 것

---

## 1. Step 2 범위 (Scope)

### ✅ 포함되는 것

- **도메인/로직**
    - QueueToken 개념 정의
    - 토큰 상태 관리: `WAITING / ELIGIBLE / EXPIRED / CONSUMED`
    - 대기열 진입, 순번(position) 계산
    - 상위 N명씩 ELIGIBLE 승급 로직
    - ReservationService에서 **EligibilityToken 검증** 추가

- **인프라**
    - Redis 연결 (Lettuce/Jedis 상관없음)
    - Redis 키 구조 설계 (ZSET + HASH 중심)

- **API**
    - `POST /api/queue/enter` – 대기열 진입 & QueueToken 발급
    - `GET /api/queue/status?token=...` – 내 대기 상태 조회
    - `POST /api/queue/promote` – 상위 N명 ELIGIBLE 승급 (관리/테스트용)

- **테스트**
    - QueueService 단위/통합 테스트
    - QueueToken 발급 동시성 테스트
    - Reservation + Queue 연동 테스트
    - k6로 `queue/enter`에 대한 간단 부하 테스트

### ❌ 포함되지 않는 것

- SSE 실시간 알림 (→ Step 3)
- Outbox / Streams / Worker (→ Step 4)
- 정산/집계, E2E k6 부하 테스트 (→ Step 5)

---

## 2. 패키지 / 모듈 구조 변화

Step 1 구조 위에 **Queue 관련 모듈**이 추가된다.

```text
src/main/java/com/example/reservation
 ├─ api
 │   ├─ queue
 │   │   └─ QueueController.java          // 대기열 엔드포인트
 │   ├─ slot
 │   └─ reservation
 ├─ application
 │   ├─ queue
 │   │   ├─ QueueService.java             // 인터페이스
 │   │   └─ QueueServiceImpl.java         // Redis 기반 구현
 │   ├─ reservation
 │   └─ idempotency
 ├─ domain
 │   ├─ queue
 │   │   ├─ QueueStatus.java              // WAITING / ELIGIBLE / EXPIRED / CONSUMED
 │   │   └─ QueueInfo.java                // queueToken, slotId, userId, status, position 등 VO
 │   ├─ slot
 │   ├─ reservation
 │   └─ idempotency
 └─ infrastructure
     ├─ persistence
     │   └─ jpa
     └─ redis
         ├─ RedisConfig.java
         └─ RedisQueueRepository.java     // Redis 연산 캡슐레이션 (선택)
