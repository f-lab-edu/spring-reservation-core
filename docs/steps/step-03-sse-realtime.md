# Step 3 – SSE 기반 실시간 대기열 알림

> **한 줄 요약**  
> Step 3에서는 **SSE(Server-Sent Events)** 를 이용해  
> “내 QueueToken 이 지금 몇 번째인지 / 입장 가능해졌는지”를  
> **실시간으로 알려주는 레이어**를 추가한다.

---

## 0. Step 3의 역할 – 전체 구조에서 어디까지 담당하나?

비유로 보면:

- Step 1: **창구(예약 코어)**
- Step 2: **번호표 기계 + 줄 세우기(Queue / Redis)**
- Step 3: **전광판 + 안내 방송(SSE 알림)**

이미 Step 2에서:

- 사용자는 `QueueToken`을 발급받고,
- `GET /api/queue/status` 로 **직접 조회(Polling)** 해서 상태를 볼 수 있다.

하지만 실제 서비스에서는:

- 브라우저가 **주기적으로 API를 때리며 상태를 조회**하는 건 비효율적이고,
- 유저 경험도 좋지 않다. (새로고침 / 스피너 / 과도한 요청)

그래서 Step 3에서:

1. **SSE 엔드포인트**를 하나 열고,
2. 클라이언트가 `QueueToken`으로 SSE에 연결하면,
3. 서버가 **Queue 상태 변화(특히 ELIGIBLE 승급)**를 실시간으로 push 해 준다.

웹소켓 대신 **SSE만 사용**하는 게 이 단계의 포인트다.

---

## 1. Step 3 범위 (Scope)

### ✅ 포함되는 것

- **SSE 인프라**
    - Spring MVC `SseEmitter` 기반 SSE 엔드포인트
    - QueueToken ↔ SseEmitter 매핑 관리
    - 타임아웃/에러/완료 시 Emitter 정리

- **알림 도메인/로직**
    - Queue 상태 변경 시 SSE 이벤트 발행
        - 예: WAITING → ELIGIBLE, EXPIRED
    - 일정 간격으로 상태 업데이트 or 클라이언트에서 “현재 상태” 요청시 응답

- **API**
    - `GET /api/queue/stream?token=...`
        - `Accept: text/event-stream`
        - 클라이언트는 `EventSource`로 구독

- **테스트**
    - SseService 단위 테스트 (Emitter 관리 로직)
    - Queue + SSE 연동 통합 테스트
    - 간단한 “동시 연결 수” 수준의 부하/스트레스 테스트 (k6로 접속만 확인)

### ❌ 포함되지 않는 것

- WebSocket
- Outbox / Streams / Worker (Step 4)
- 정산/집계 & E2E 부하 테스트 (Step 5)

---

## 2. 패키지 / 모듈 구조 변경

기존 Step 1, 2 구조 위에 **sse 모듈**을 추가한다.

```text
src/main/java/com/example/reservation
 ├─ api
 │   ├─ queue
 │   │   ├─ QueueController.java          // Step 2
 │   │   └─ QueueSseController.java       // Step 3 – SSE 엔드포인트
 │   ├─ reservation
 │   └─ slot
 ├─ application
 │   ├─ queue
 │   ├─ reservation
 │   ├─ idempotency
 │   └─ sse
 │       ├─ QueueSseService.java          // 인터페이스
 │       └─ QueueSseServiceImpl.java      // SseEmitter 관리 구현
 ├─ domain
 │   ├─ queue
 │   │   ├─ QueueStatus.java
 │   │   └─ QueueInfo.java
 │   ├─ sse
 │   │   └─ QueueSseEventType.java        // (선택) 이벤트 타입 정의
 │   ├─ reservation
 │   └─ slot
 └─ infrastructure
     ├─ persistence
     │   └─ jpa
     └─ redis
         └─ RedisQueueRepository.java
