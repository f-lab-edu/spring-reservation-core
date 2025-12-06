# Step 4 – Outbox + Redis Streams + Worker (비동기 후처리)

> **한 줄 요약**  
> Step 4에서는 **예약 성공 이후의 “후처리(정산 카운트, 로그, 알림 등)”를  
> HTTP 요청 흐름에서 분리**하기 위해  
> **Outbox + Redis Streams + Worker** 기반의 비동기 처리 레이어를 도입한다.

---

## 0. Step 4의 역할 – 전체 구조에서 어디까지 담당하나?

지금까지:

- Step 1: **예약 코어 (DB, 트랜잭션, 멱등성)**
- Step 2: **Redis 대기열 (QueueToken / EligibilityToken)**
- Step 3: **SSE 기반 실시간 대기열 알림**

하지만 현실 서비스에서는:

- 예약이 성공한 뒤에 다양한 **후처리**가 필요하다.
    - 예: 정산 카운트 증가, 로그 적재, 알림 발송, 추천 시스템 기록 등
- 이 모든걸 HTTP 요청 안에서 동기로 처리하면:
    - API 응답이 느려지고,
    - 후처리 실패로 인해 예약까지 롤백하거나,
    - 외부 서비스 장애가 예약에도 영향을 줄 수 있다.

그래서 Step 4에서는:

1. **“예약 성공”까지만 동기로 처리**하고
2. 그 이후는 **이벤트로 기록 → Redis Streams → Worker가 비동기 처리**하는 패턴을 구현한다.

핵심 키워드:

- Outbox 패턴
- Redis Streams / Consumer Group
- Worker / Inbox / 멱등성
- DLQ (Dead Letter Queue) / Retry 정책

---

## 1. Step 4 범위 (Scope)

### ✅ 포함되는 것

- **Outbox 도메인 & DB 테이블**
    - `OutboxEvent` 엔티티/테이블
    - 예약 성공 시 Outbox에 이벤트 기록

- **Outbox → Redis Streams Relay**
    - 정기적으로 Outbox 테이블을 읽어서 Redis Streams로 publish
    - 성공적으로 publish된 레코드는 Outbox에서 처리 상태 갱신/삭제

- **Worker (Redis Streams Consumer)**
    - Redis Streams에서 이벤트를 읽어 후처리 수행
    - 예: 예약 성공 이벤트를 기반으로 정산 카운트 증가 등

- **Inbox(멱등 처리)**
    - Worker 쪽에서 **같은 이벤트를 여러 번 받아도 한번만 처리**되도록 하는 멱등성 레이어

- **에러/Retry/DLQ 전략**
    - 재시도 횟수 초과 시 DLQ로 보낸다거나,
    - 실패 이벤트를 별도 테이블/Stream에 적재

### ❌ 포함되지 않는 것

- 정산 테이블/배치 + E2E k6 (→ Step 5에서 마무리)
- 고급 모니터링/대시보드 구성 (Prometheus/Grafana 세부 설정 등)

---

## 2. 패키지 / 모듈 구조 확장

```text
src/main/java/com/example/reservation
 ├─ api
 │   ├─ queue
 │   ├─ reservation
 │   └─ slot
 ├─ application
 │   ├─ queue
 │   ├─ reservation
 │   ├─ idempotency
 │   ├─ sse
 │   └─ event
 │       ├─ ReservationEventPublisher.java   // 예약 도메인에서 Outbox에 쓰는 인터페이스
 │       └─ ReservationEventHandler.java     // Worker가 실제 비즈니스 후처리 담당
 ├─ domain
 │   ├─ outbox
 │   │   ├─ OutboxEvent.java
 │   │   ├─ OutboxStatus.java                // PENDING / PUBLISHED / FAILED
 │   │   └─ OutboxEventRepository.java
 │   ├─ inbox
 │   │   ├─ InboxEvent.java
 │   │   ├─ InboxStatus.java                 // PROCESSED 등
 │   │   └─ InboxEventRepository.java
 │   ├─ event
 │   │   └─ ReservationEventType.java        // "RESERVATION_CONFIRMED" 등
 │   ├─ reservation
 │   ├─ queue
 │   ├─ sse
 │   └─ slot
 └─ infrastructure
     ├─ persistence
     │   ├─ jpa
     │   │   ├─ JpaOutboxEventRepository.java
     │   │   └─ JpaInboxEventRepository.java
     │   └─ ...
     └─ redis
         ├─ RedisConfig.java
         └─ RedisStreamsClient.java          // XADD / XREADGROUP 등 캡슐레이션
