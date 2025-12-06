## 1. 작업 개요 (Summary)

> 이 PR이 해결하는 문제 / 추가된 기능을 한 줄로 요약해 주세요.

- 예) Step 1 예약 엔티티 및 기본 생성 API 구현

---

## 2. 상세 내용 (What & How)

### 2-1. 주요 변경 사항

- [ ] 도메인 / 엔티티
    - 예) `Reservation` 엔티티 신규 추가 (userId, seatId, status, version 등)
- [ ] 서비스 / 비즈니스 로직
    - 예) 동시성 제어를 위해 비관적 락 사용 (`@Lock(PESSIMISTIC_WRITE)`)
- [ ] API / 컨트롤러
    - 예) `POST /api/reservations` 예약 생성 엔드포인트 추가
- [ ] 기타
    - 예) 예외 타입 정의, 공통 에러 응답 포맷 추가

### 2-2. 설계/의도 (Design decisions)

- 왜 이렇게 설계했는지, 다른 대안은 뭐였는지 간단히 적어 주세요.
- 예)
    - 초기에는 낙관적 락을 고려했지만, Step 1에서는 단순화를 위해 비관적 락을 사용
    - 좌석 수가 적고, 긴 트랜잭션이 없다고 가정하여 Deadlock 위험이 낮다고 판단

### 2-3. DB / 마이그레이션

- 변경된 스키마 / 엔티티 필드가 있다면 정리해 주세요.

예)
```sql
create table reservations (
    id           bigint auto_increment primary key,
    user_id      bigint      not null,
    seat_id      bigint      not null,
    status       varchar(20) not null,
    version      bigint      not null,
    created_at   datetime(6) not null,
    updated_at   datetime(6) not null
);
