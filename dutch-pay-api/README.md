# N빵 (N-Bang)

모임별 정산을 쉽게 관리할 수 있는 N빵 서비스입니다.

여행, 회식 등 모임에서 발생하는 지출을 기록하고, 참여자별 정산 금액을 자동으로 계산해줍니다.

---

## Front

> 추후 개발 예정

---

## API

### 기술 스택

- Kotlin 2.1
- Spring Boot 4.x
- Spring Data JPA
- PostgreSQL
- Testcontainers

---

### 아키텍처

레이어드 아키텍처 기반으로 구성되어 있으며, 도메인 주도 설계(DDD) 원칙을 적용했습니다.

```
src/main/kotlin/com/nbang/nbangapi/
├── domain/                    # 도메인 계층 (엔티티, 순수 Repository 인터페이스)
│   ├── gathering/
│   ├── participant/
│   ├── round/
│   ├── exclusion/
│   └── common/
├── application/               # 애플리케이션 계층 (서비스, DTO)
│   ├── gathering/
│   ├── participant/
│   ├── round/
│   └── calculation/
├── infrastructure/            # 인프라 계층 (JPA Repository 구현체)
│   ├── gathering/
│   ├── participant/
│   ├── round/
│   └── exclusion/
├── interfaces/                # 인터페이스 계층 (Controller)
│   └── api/
└── support/                   # 공통 지원 (에러 처리 등)
    └── error/
```

**계층별 역할**

| 계층             | 역할                             |
|----------------|--------------------------------|
| Domain         | 비즈니스 핵심 로직, 엔티티, 순수 인터페이스 정의   |
| Application    | 유스케이스 구현, 트랜잭션 관리, DTO 변환      |
| Infrastructure | 기술적 구현 (JPA Repository, 외부 연동) |
| Interfaces     | HTTP 요청/응답 처리, API 엔드포인트       |

---

### ERD

```
┌─────────────────────┐
│     gatherings      │
├─────────────────────┤
│ id (PK, BIGINT)     │
│ name                │
│ start_date          │
│ end_date            │
│ created_at          │
│ updated_at          │
└─────────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────────┐       ┌─────────────────────┐
│    participants     │       │  settlement_rounds  │
├─────────────────────┤       ├─────────────────────┤
│ id (PK, BIGINT)     │       │ id (PK, BIGINT)     │
│ name                │◄──────│ payer_id (FK)       │
│ gathering_id (FK)   │       │ gathering_id (FK)   │
│ created_at          │       │ title               │
│ updated_at          │       │ amount              │
└─────────────────────┘       │ receipt_image_url   │
         ▲                    │ created_at          │
         │                    │ updated_at          │
         │                    └─────────────────────┘
         │                             │
         │                             │ 1:N
         │                             ▼
         │                    ┌─────────────────────┐
         │                    │     exclusions      │
         │                    ├─────────────────────┤
         └────────────────────│ participant_id (FK) │
                              │ id (PK, BIGINT)     │
                              │ round_id (FK)       │
                              │ reason              │
                              │ created_at          │
                              │ updated_at          │
                              └─────────────────────┘
```

**테이블 설명**

| 테이블               | 설명                            |
|-------------------|-------------------------------|
| gatherings        | 모임 정보 (여행, 회식 등)              |
| participants      | 모임 참여자                        |
| settlement_rounds | 정산 차수 (1차 고기집, 2차 카페 등)       |
| exclusions        | 특정 차수에서 제외된 참여자 (불참, 술 안마심 등) |

---

### 기능 정의서

#### 1. 모임 관리

| 기능       | Method | Endpoint               | 설명                          |
|----------|--------|------------------------|-----------------------------|
| 모임 생성    | POST   | `/api/gatherings`      | 모임 생성 및 참여자 등록              |
| 모임 목록 조회 | GET    | `/api/gatherings`      | 전체 모임 목록 조회                 |
| 모임 상세 조회 | GET    | `/api/gatherings/{id}` | 모임 상세 정보 조회 (참여자, 정산 차수 포함) |
| 모임 수정    | PATCH  | `/api/gatherings/{id}` | 모임 정보 수정                    |
| 모임 삭제    | DELETE | `/api/gatherings/{id}` | 모임 삭제                       |

#### 2. 참여자 관리

| 기능        | Method | Endpoint                                     | 설명            |
|-----------|--------|----------------------------------------------|---------------|
| 참여자 추가    | POST   | `/api/gatherings/{gatheringId}/participants` | 모임에 참여자 추가    |
| 참여자 목록 조회 | GET    | `/api/gatherings/{gatheringId}/participants` | 모임의 참여자 목록 조회 |
| 참여자 삭제    | DELETE | `/api/participants/{id}`                     | 참여자 삭제        |

#### 3. 정산 차수 관리

| 기능          | Method | Endpoint                               | 설명                         |
|-------------|--------|----------------------------------------|----------------------------|
| 정산 차수 생성    | POST   | `/api/gatherings/{gatheringId}/rounds` | 정산 차수 생성 (결제자, 금액, 제외자 지정) |
| 정산 차수 목록 조회 | GET    | `/api/gatherings/{gatheringId}/rounds` | 모임의 정산 차수 목록 조회            |
| 정산 차수 상세 조회 | GET    | `/api/rounds/{id}`                     | 정산 차수 상세 정보 조회             |
| 정산 차수 수정    | PATCH  | `/api/rounds/{id}`                     | 정산 차수 수정                   |
| 정산 차수 삭제    | DELETE | `/api/rounds/{id}`                     | 정산 차수 삭제                   |
| 영수증 이미지 업로드 | POST   | `/api/rounds/{id}/image`               | 정산 차수에 영수증 이미지 첨부          |

#### 4. 정산 계산

| 기능       | Method | Endpoint                         | 설명              |
|----------|--------|----------------------------------|-----------------|
| 정산 결과 계산 | GET    | `/api/gatherings/{id}/calculate` | 모임의 최종 정산 결과 계산 |

**정산 계산 응답 예시**

```json
{
  "gatheringId": 1,
  "gatheringName": "제주도 여행",
  "totalAmount": 105000,
  "balances": [
    {
      "participantId": 1,
      "name": "홍길동",
      "totalPaid": 60000,
      "totalOwed": 30000,
      "netBalance": 30000
    },
    {
      "participantId": 2,
      "name": "김철수",
      "totalPaid": 30000,
      "totalOwed": 37500,
      "netBalance": -7500
    }
  ],
  "debts": [
    {
      "from": "김철수",
      "to": "홍길동",
      "amount": 7500
    }
  ]
}
```

---

### 실행 방법

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트 (Docker 필요 - Testcontainers 사용)
./gradlew test
```
