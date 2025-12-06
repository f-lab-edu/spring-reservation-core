# Reservation Queue System

> **한 줄 요약**  
> “오픈런 예약(콘서트 예매, 티켓팅, 한정판 판매)” 상황을 가정해서  
> **대기열 + 예약 정합성 + 실시간 알림 + 비동기 처리 + 정산/모니터링**까지  
> 하나의 프로젝트 안에서 단계적으로 구현·학습하는 백엔드 시스템.

---

## 1. 이 프로젝트는 무엇을 위한 건가?

### 1.1 배경 – 현실에서 자주 등장하는 시나리오

현실 서비스에서 자주 등장하는 문제들:

- 인기 콘서트 티켓 예매
- 한정 수량 굿즈 판매
- 특정 시간대에 몰리는 예약(병원, 클래스, 스터디룸 등)

이런 상황에서는 거의 항상 같은 문제가 반복된다.

1. **수천 명이 동시에 버튼을 누른다.**
2. DB에 초당 수백~수천 개의 예약/주문 요청이 몰린다.
3. 잘못 설계하면:
   - 초과예약 / 중복예약
   - 같은 요청이 여러 번 처리 (멱등성 실패)
   - DB 부하 폭발 / 타임아웃 / 서버 다운
4. 이후 운영 단계에서:
   - “정확히 몇 명이 예약됐는지” 확인하기 어렵고
   - 장애가 나도 모니터링/로그가 제대로 안 잡혀서 원인 분석이 힘들다.

---

### 1.2 이 프로젝트가 해결하려는 핵심 문제

이 프로젝트의 목적은, 단순히 “예약 CRUD” 만드는 게 아니라:

1. **“대량 동시 트래픽에서도 무너지지 않는 예약 시스템”을 설계·구현하는 연습**
2. **실제 코드와 아키텍처로 설명할 수 있게 만드는 것**

   예를 들어:

   - 동시성 제어 (DB 락, 낙관락/비관락)
   - 멱등성 (Idempotency-Key, Outbox/Inbox)
   - 대기열 설계 (Redis, Token 기반 Queue)
   - 비동기 처리 (이벤트, Worker, 메시지 스트림)
   - 분산 환경에서의 정합성/신뢰성
   - 모니터링, 지표, 부하 테스트

3. **현실적인 아키텍처 진화 과정까지 경험**

   - 처음부터 “이론적인 MSA”를 만드는 대신,
   - **모놀리스 → 모듈러 모놀리스 → (필요하면) 서비스 분리**가 가능한 구조를 단계적으로 밟아본다.

---

## 2. 이 프로젝트가 제공하는 것 (기능 관점)

### 2.1 기능 요약

1. **예약 코어**
   - Slot(예약 가능한 시간/자원) 관리
   - Reservation(예약 결과) 관리
   - 동시성 제어 + 초과예약 방지
   - 멱등성(Idempotency-Key) 처리

2. **대기열(Queue)**
   - Redis 기반 QueueToken 발급
   - 순번/상태(WAITING, ELIGIBLE, EXPIRED, CONSUMED) 관리
   - ELIGIBLE 상태만 실제 예약 가능

3. **실시간 알림(SSE)**
   - 사용자는 자신의 QueueToken으로 SSE 구독
   - “지금 몇 번째인지 / 언제 입장 가능한지”를 서버가 푸시

4. **비동기 처리 (Outbox + Redis Streams + Worker)**
   - 예약 성공 이후의 작업(정산 카운팅, 알림, 결제 모사 등)을 HTTP 요청 흐름에서 분리
   - Outbox에 이벤트 기록 → Redis Streams로 발행 → Worker가 비동기 처리
   - Inbox / 멱등 처리로 중복 이벤트에도 안전하게 동작

5. **정산·집계 + 모니터링·부하 테스트**
   - Redis 카운터 기반 집계 → MySQL 정산 테이블에 주기적 Flush
   - 정산 조회 API
   - Spring Actuator + Prometheus + Grafana 기반 모니터링
   - k6 부하 테스트 스크립트로 오픈런/대기열/예약 시나리오 테스트

---

## 3. 이 프로젝트가 다루는 주제 (기술 관점)

**실무에서 자주 나오는 테마**들을 한 번에 아우르도록 설계했다.

- **동시성 & 트랜잭션**
  - 동일 Slot에 수많은 예약 요청이 몰릴 때, 초과예약 없이 처리
  - 낙관적 락, 비관적 락, 트랜잭션 경계 설계

- **멱등성**
  - Idempotency-Key 기반 HTTP 멱등성
  - Outbox / Inbox를 통한 이벤트 처리 멱등성

- **대기열 설계**
  - Redis ZSET / Hash를 사용한 QueueToken 관리
  - QueueToken 상태 관리 및 입장 조건 판단
  - API 앞단에 Queue를 둬서 DB 보호

- **실시간 알림 (SSE)**
  - Polling vs SSE 비교
  - SSE 연결 관리, 세션/Emitter 관리 방식

- **비동기 처리 / 이벤트 기반 아키텍처**
  - Outbox 패턴(트랜잭션 내 이벤트 기록)
  - Redis Streams + Consumer Group 기반 Worker 설계
  - DLQ, Retry/Backoff, Inbox 멱등성

- **정산/집계, 모니터링, 부하 테스트**
  - 카운터 집계 → 정산 테이블 저장
  - Actuator/Prometheus/Grafana로 지표 노출
  - k6로 시나리오 부하 테스트 설계

---

## 4. 아키텍처 개요

### 4.1 한눈에 보는 데이터 흐름

**1) 기본 예약 플로우 (Queue 포함 최종 형태 기준)**

1. 사용자가 **대기열 진입** 요청 → Redis에서 QueueToken 발급
2. 사용자 브라우저는 QueueToken으로 SSE 구독
3. 서버가 QueueToken을 **ELIGIBLE**로 승격 → SSE로 “입장 가능” 이벤트 전송
4. 사용자가 EligibilityToken과 함께 **예약 API 호출**
5. Reservation 코어가:
   - Slot에 대해 동시성 제어로 remaining 감소
   - Reservation row 생성
   - Outbox 이벤트 기록
6. Outbox Relay가 Outbox → Redis Streams로 이벤트 퍼블리시
7. Worker가 이벤트 소비:
   - 정산 카운터 증가
   - 알림/로그/핸들러 실행
8. 정산 배치가 Redis 카운터 → MySQL 정산 테이블로 Flush
9. 정산 조회 API + 모니터링 대시보드 + k6 부하 테스트 결과로 최종 검증

### 4.2 레이어 구조 (모듈러 모놀리스)

```text
com.example.reservation
 ├─ api              // REST, SSE, DTO, Controller
 ├─ application      // 유스케이스 / 서비스 로직
 ├─ domain           // 도메인 모델, 비즈니스 규칙, Repository 인터페이스
 └─ infrastructure   // JPA, Redis, Streams, 외부 연동, 설정
```

---

## Tech Stack

### Language & Runtime
- **Java 17**
- **Gradle (Groovy DSL)**

### Framework
- **Spring Boot 3.5.8**
    - `spring-boot-starter-web`
    - `spring-boot-starter-data-jpa`
    - `spring-boot-starter-validation`

### Database & Persistence
- **MySQL** (InnoDB 기준)
- **Spring Data JPA**

### Utilities
- **Lombok**

### Testing
- `spring-boot-starter-test`
- `testcontainers:mysql`
- `testcontainers:junit-jupiter`

---

## Step 문서

- [Step 1 – 예약 코어 설계](docs/steps/step-01-core-reservation.md)
- [Step 2 – 대기열/토큰 설계](docs/steps/step-02-waiting-queue.md)
- [Step 3 – SSE 실시간 알림](docs/steps/step-03-sse-realtime.md)
- [Step 4 – Outbox + Worker 비동기 처리](docs/steps/step-04-outbox-worker.md)
- [Step 5 – 모니터링 & 부하 테스트](docs/steps/step-05-monitoring-and-test.md)
