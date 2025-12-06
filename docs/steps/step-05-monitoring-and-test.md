# Step 5 – 정산·집계 + 모니터링 + E2E 부하 테스트(k6)

> **한 줄 요약**  
> Step 5에서는 지금까지 만든 **예약 + 대기열 + SSE + Outbox/Worker** 시스템 위에  
> **정산/집계, 모니터링, E2E 부하 테스트(k6)**를 얹어서  
> “운영 가능한 수준으로 검증된 시스템”으로 마무리한다.

---

## 0. Step 5의 역할 – 전체 프로젝트에서 어디까지 담당하나?

지금까지:

- Step 1 – **예약 코어**: Slot/Reservation + 트랜잭션 + 동시성 + 멱등성
- Step 2 – **대기열**: Redis QueueToken / EligibilityToken
- Step 3 – **실시간 알림**: SSE로 Queue 상태/승급 알림
- Step 4 – **비동기 후처리**: Outbox → Redis Streams → Worker

하지만 운영 관점에서 보면 아직 몇 가지가 부족하다:

1. “**얼마나 예약이 되었는지 / Slot 별로 얼마나 팔렸는지 / 일별 집계**” 같은 **정산 관점**의 숫자
2. 장애/병목/부하 상황에서 **지표(Metrics)와 모니터링**이 없다.
3. 지금까지의 k6는 부분적으로만 때려봤고,  
   **“Queue → SSE → Reservation → Outbox → Worker → 정산까지 이어지는 전체 플로우”**를  
   한 번에 두드려보지 않았다.

그래서 Step 5의 목표는:

- **정산/집계 레이어**를 추가하고
- **Spring Actuator + Prometheus + Grafana**로 모니터링을 붙이고
- **k6로 E2E 시나리오 부하 테스트**를 수행해서

> “이 정도면 트래픽이 몰려도 어떻게 동작하는지, 어디가 병목인지 설명할 수 있다”  
> 수준까지 끌어올리는 것.

---

## 1. Step 5 범위 (Scope)

### ✅ 포함되는 것

1. **정산/집계**
    - 실시간 카운터(예: Redis)에 쌓인 예약 수를
    - 배치/잡을 통해 **정산 테이블**에 주기적으로 Flush
    - 정산 조회 API (Simple Reporting API)

2. **모니터링**
    - Spring Actuator + Micrometer
    - Prometheus 스크레이핑 설정
    - (옵션) Grafana 대시보드 예시 구조

3. **E2E 부하 테스트 (k6)**
    - “오픈런 티켓팅” 전체 시나리오를 k6 스크립트로 작성
    - VU 증가에 따른 응답 시간 / 실패율 / 예약 성공/실패 수 / 정산 수치 검증

### ❌ 포함되지 않는 것

- 완전한 BI/리포트 시스템 (복잡한 쿼리, UI 등)
- 아주 정교한 알람 규칙/슬랙 연동 등 (필요하면 설계 수준으로만 적어도 됨)

---

## 2. 정산/집계 설계

### 2.1 개념

- **Worker(ReservationWorker)**는 Step 4에서 이미  
  “예약 성공” 이벤트를 받아서 **슬롯별 카운터를 증가**시키도록 설계할 수 있다.
    - 예: `slot_reservation_count:{slotId}` 같은 Redis key에 `INCR`
- 하지만 Redis는 휘발성이므로:
    - 일정 주기로 **DB(정산 테이블)**에 집계 값을 Flush 해줘야 한다.

정산 설계 방향:

1. **실시간 카운터 (Redis)**
    - 빠르게 읽고/쓰는 데 최적화
    - 단기 통계, Worker가 사용하는 집계용

2. **정산 테이블 (MySQL)**
    - “일별 / Slot별 예약 수, 매출” 같은 영구 데이터 저장
    - 운영자/운영툴/리포트에서 조회

---

### 2.2 Redis 카운터 구조

예시 키:

```text
stats:slot:{slotId}:reservations:day:{yyyyMMdd}  -> 예약 수
stats:slot:{slotId}:revenue:day:{yyyyMMdd}       -> 매출 (필요 시)
