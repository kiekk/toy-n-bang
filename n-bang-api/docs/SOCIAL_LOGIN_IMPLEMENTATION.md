# 소셜 로그인 기능 구현 계획

## 목차

1. [ERD](#1-erd)
2. [구현 체크리스트](#2-구현-체크리스트)
3. [기술 스택](#3-기술-스택)
4. [상세 설계](#4-상세-설계)
5. [API 명세](#5-api-명세)
6. [설정 파일](#6-설정-파일)

---

## 1. ERD

### 현재 ERD

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    현재 ERD                                             │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   gatherings    │       │  participants   │       │ settlement_     │
├─────────────────┤       ├─────────────────┤       │    rounds       │
│ id (PK)         │◀──┐   │ id (PK)         │   ┌──▶├─────────────────┤
│ name            │   │   │ name            │   │   │ id (PK)         │
│ start_date      │   └───│ gathering_id(FK)│   │   │ title           │
│ end_date        │       │ created_at      │   │   │ amount          │
│ created_at      │       │ updated_at      │   │   │ payer_id (FK)───┼───┐
│ updated_at      │       └─────────────────┘   │   │ gathering_id(FK)│───┼──▶ gatherings
└─────────────────┘               │             │   │ receipt_image_  │   │
                                  │             │   │   url           │   │
                                  │             │   │ created_at      │   │
                                  │             │   │ updated_at      │   │
                                  │             │   └─────────────────┘   │
                                  │             │                         │
                                  │             │   ┌─────────────────┐   │
                                  │             │   │   exclusions    │   │
                                  │             │   ├─────────────────┤   │
                                  └─────────────┼───│ participant_id  │   │
                                                │   │   (FK)          │   │
                                                └───│ round_id (FK)   │   │
                                                    │ reason          │   │
                                                    │ created_at      │   │
                                                    │ updated_at      │   │
                                                    └─────────────────┘   │
                                                            participants◀─┘
```

### 변경 후 ERD

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    변경 후 ERD                                          │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐
│    members      │  ◀── 신규 테이블
├─────────────────┤
│ id (PK)         │
│ email           │
│ nickname        │
│ profile_image   │
│ provider        │──── GOOGLE, KAKAO
│ provider_id     │──── 소셜 서비스 고유 ID
│ role            │──── USER, ADMIN
│ created_at      │
│ updated_at      │
└────────┬────────┘
         │
         │ 1:N
         │
         ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   gatherings    │       │  participants   │       │ settlement_     │
├─────────────────┤       ├─────────────────┤       │    rounds       │
│ id (PK)         │◀──┐   │ id (PK)         │   ┌──▶├─────────────────┤
│ member_id (FK)  │◀─┐│   │ name            │   │   │ id (PK)         │
│ name            │  ││   │ gathering_id(FK)│───┘   │ title           │
│ start_date      │  │└───│ created_at      │       │ amount          │
│ end_date        │  │    │ updated_at      │       │ payer_id (FK)   │
│ created_at      │  │    └─────────────────┘       │ gathering_id(FK)│
│ updated_at      │  │            │                 │ receipt_image_  │
└─────────────────┘  │            │                 │   url           │
         │           │            │                 │ created_at      │
         │           │            │                 │ updated_at      │
     members         │            │                 └─────────────────┘
                     │            │                         │
                     │            │                         │
                     │            │   ┌─────────────────┐   │
                     │            │   │   exclusions    │   │
                     │            │   ├─────────────────┤   │
                     │            └───│ participant_id  │   │
                     │                │   (FK)          │   │
                     │                │ round_id (FK)───┼───┘
                     │                │ reason          │
                     │                │ created_at      │
                     │                │ updated_at      │
                     │                └─────────────────┘
                     │
                     │
┌────────────────────┴────┐
│    refresh_tokens       │  ◀── 신규 테이블 (선택)
├─────────────────────────┤
│ id (PK)                 │
│ member_id (FK)          │
│ token                   │
│ expires_at              │
│ created_at              │
└─────────────────────────┘
```

### 테이블 상세

#### members (신규)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 회원 ID |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 이메일 |
| nickname | VARCHAR(100) | NOT NULL | 닉네임 |
| profile_image | VARCHAR(500) | NULLABLE | 프로필 이미지 URL |
| provider | VARCHAR(20) | NOT NULL | OAuth Provider (GOOGLE, KAKAO) |
| provider_id | VARCHAR(255) | NOT NULL | Provider별 고유 ID |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | 권한 (USER, ADMIN) |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 |

**인덱스**:
- UNIQUE INDEX `uk_members_provider` (provider, provider_id)
- UNIQUE INDEX `uk_members_email` (email)

#### gatherings (수정)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| **member_id** | BIGINT | NOT NULL, FK | 모임 생성자 (신규) |
| ... | ... | ... | 기존 컬럼 유지 |

#### refresh_tokens (신규, 선택)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 토큰 ID |
| member_id | BIGINT | NOT NULL, FK | 회원 ID |
| token | VARCHAR(500) | NOT NULL, UNIQUE | Refresh Token |
| expires_at | TIMESTAMP | NOT NULL | 만료일시 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |

---

## 2. 구현 체크리스트

### Phase 1: 기반 설정 ✅ 완료

```
[x] 1.1 의존성 추가
    [x] spring-boot-starter-oauth2-client
    [x] spring-boot-starter-security
    [x] jjwt-api, jjwt-impl, jjwt-jackson (JWT)

[x] 1.2 설정 파일 작성
    [x] application.yml - OAuth2 Client 설정
    [x] application.yml - JWT 설정 (secret, expiration)
```

### Phase 2: Member 도메인 ✅ 완료

```
[x] 2.1 Entity 생성
    [x] Member.kt
    [x] OAuthProvider.kt (enum)
    [x] Role.kt (enum)

[x] 2.2 Repository 생성
    [x] MemberRepository.kt
    [x] MemberRepositoryImpl.kt
    [x] MemberJpaRepository.kt

[x] 2.3 Service 생성
    [x] MemberService.kt
```

### Phase 3: OAuth2 로그인 ✅ 완료

```
[x] 3.1 OAuth2 설정
    [x] SecurityConfig.kt
    [x] OAuth2UserInfo.kt (인터페이스)
    [x] GoogleOAuth2UserInfo.kt
    [x] KakaoOAuth2UserInfo.kt
    [x] OAuth2UserInfoFactory.kt (OAuth2UserInfo 내 companion object)

[x] 3.2 OAuth2 Service
    [x] CustomOAuth2UserService.kt
    [x] OAuth2AuthenticationSuccessHandler.kt
    [x] OAuth2AuthenticationFailureHandler.kt
```

### Phase 4: JWT 인증 ✅ 완료

```
[x] 4.1 JWT 유틸리티
    [x] JwtTokenProvider.kt
    [x] JwtProperties.kt

[x] 4.2 JWT 필터
    [x] JwtAuthenticationFilter.kt

[x] 4.3 인증 관련
    [x] UserPrincipal.kt
    [x] CurrentUser.kt (어노테이션)
    [x] SecurityConfig에서 ArgumentResolver 설정
```

### Phase 5: Gathering 수정 ✅ 완료

```
[x] 5.1 Entity 수정
    [x] Gathering.kt - memberId 필드 추가

[x] 5.2 Repository 수정
    [x] GatheringRepository.kt - findAllByMemberId 추가

[x] 5.3 Service 수정
    [x] GatheringService.kt - 소유자 검증 로직 추가

[x] 5.4 Facade 수정
    [x] GatheringFacade.kt - 인증된 사용자 정보 활용

[x] 5.5 Controller 수정
    [x] GatheringController.kt - @CurrentUser 적용
```

### Phase 6: API 보안 ✅ 완료

```
[x] 6.1 인증 필요 API 설정
    [x] SecurityConfig - 인증 필요 경로 설정

[x] 6.2 인가 로직
    [x] 본인 모임만 접근 가능하도록 검증
```

### Phase 7: 테스트 ✅ 완료

```
[x] 7.1 기존 테스트 수정
    [x] 모든 테스트에 memberId 파라미터 추가
    [x] E2ETest에 JWT 인증 추가
    [x] 테스트용 application.yml 설정

[x] 7.2 통합 테스트
    [x] 95개 전체 테스트 통과
```

### Phase 8: 문서화

```
[ ] 8.1 API 문서 업데이트
[ ] 8.2 배포 문서 업데이트 (OAuth2 설정 추가)
```

---

## 3. 기술 스택

### 의존성

```kotlin
// build.gradle.kts
dependencies {
    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Test
    testImplementation("org.springframework.security:spring-security-test")
}
```

---

## 4. 상세 설계

### 인증 흐름

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              OAuth2 로그인 흐름                                          │
└─────────────────────────────────────────────────────────────────────────────────────────┘

    Client                    Backend                   OAuth Provider
       │                         │                            │
       │  1. 로그인 요청          │                            │
       │ ───────────────────────▶│                            │
       │  GET /oauth2/authorize/ │                            │
       │      google (or kakao)  │                            │
       │                         │                            │
       │  2. Redirect            │                            │
       │ ◀───────────────────────│                            │
       │  → Provider 로그인 페이지│                            │
       │                         │                            │
       ├─────────────────────────┼────────────────────────────▶
       │  3. 사용자 로그인/동의   │                            │
       │◀────────────────────────┼────────────────────────────┤
       │  4. Redirect with code  │                            │
       │                         │                            │
       │ ────────────────────────▶  5. Authorization Code     │
       │                         │ ───────────────────────────▶
       │                         │                            │
       │                         │  6. Access Token           │
       │                         │ ◀───────────────────────────
       │                         │                            │
       │                         │  7. 사용자 정보 요청        │
       │                         │ ───────────────────────────▶
       │                         │                            │
       │                         │  8. 사용자 정보             │
       │                         │ ◀───────────────────────────
       │                         │                            │
       │                         │  9. Member 조회/생성       │
       │                         │  10. JWT 토큰 생성         │
       │                         │                            │
       │  11. Redirect           │                            │
       │ ◀───────────────────────│                            │
       │  → Frontend + Token     │                            │
       │                         │                            │
```

### JWT 인증 흐름

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              JWT 인증 흐름                                               │
└─────────────────────────────────────────────────────────────────────────────────────────┘

    Client                    JwtAuthenticationFilter           Controller
       │                              │                              │
       │  API 요청                    │                              │
       │  Authorization: Bearer xxx   │                              │
       │ ────────────────────────────▶│                              │
       │                              │                              │
       │                              │  1. 토큰 추출                │
       │                              │  2. 토큰 검증                │
       │                              │  3. 사용자 정보 조회         │
       │                              │  4. SecurityContext 설정     │
       │                              │                              │
       │                              │ ────────────────────────────▶│
       │                              │                              │
       │                              │                              │  5. 비즈니스 로직
       │                              │                              │     처리
       │                              │                              │
       │                              │ ◀────────────────────────────│
       │ ◀────────────────────────────│                              │
       │  Response                    │                              │
       │                              │                              │
```

### 프로젝트 구조

```
src/main/kotlin/com/nbang/nbangapi/
├── domain/
│   ├── member/
│   │   ├── Member.kt
│   │   ├── MemberRepository.kt
│   │   ├── MemberService.kt
│   │   ├── OAuthProvider.kt
│   │   └── Role.kt
│   └── ...
│
├── infrastructure/
│   ├── member/
│   │   ├── MemberJpaRepository.kt
│   │   └── MemberRepositoryImpl.kt
│   └── ...
│
├── application/
│   ├── auth/
│   │   ├── AuthFacade.kt
│   │   └── response/
│   │       └── TokenResponse.kt
│   └── ...
│
├── interfaces/
│   └── api/
│       ├── auth/
│       │   └── AuthController.kt
│       └── ...
│
└── support/
    ├── security/
    │   ├── SecurityConfig.kt
    │   ├── JwtTokenProvider.kt
    │   ├── JwtAuthenticationFilter.kt
    │   ├── JwtProperties.kt
    │   ├── UserPrincipal.kt
    │   ├── CurrentUser.kt
    │   └── CurrentUserArgumentResolver.kt
    │
    └── oauth2/
        ├── CustomOAuth2UserService.kt
        ├── OAuth2AuthenticationSuccessHandler.kt
        ├── OAuth2AuthenticationFailureHandler.kt
        ├── OAuth2UserInfo.kt
        ├── GoogleOAuth2UserInfo.kt
        ├── KakaoOAuth2UserInfo.kt
        └── OAuth2UserInfoFactory.kt
```

---

## 5. API 명세

### 인증 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/oauth2/authorize/google` | Google 로그인 시작 | X |
| GET | `/oauth2/authorize/kakao` | Kakao 로그인 시작 | X |
| GET | `/oauth2/callback/{provider}` | OAuth2 콜백 (자동) | X |
| POST | `/api/v1/auth/refresh` | Access Token 갱신 | Refresh Token |
| POST | `/api/v1/auth/logout` | 로그아웃 | O |
| GET | `/api/v1/auth/me` | 내 정보 조회 | O |

### 토큰 응답

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 기존 API 변경

| Method | Endpoint | 변경 사항 |
|--------|----------|-----------|
| GET | `/api/v1/gatherings` | 로그인한 사용자의 모임만 조회 |
| POST | `/api/v1/gatherings` | memberId 자동 설정 |
| GET | `/api/v1/gatherings/{id}` | 본인 모임만 접근 가능 |
| PUT | `/api/v1/gatherings/{id}` | 본인 모임만 수정 가능 |
| DELETE | `/api/v1/gatherings/{id}` | 본인 모임만 삭제 가능 |

---

## 6. 설정 파일

### application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile

          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/oauth2/callback/{registrationId}"
            scope:
              - profile_nickname
              - profile_image
              - account_email

        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 3600000      # 1시간 (밀리초)
  refresh-token-expiration: 604800000   # 7일 (밀리초)

app:
  oauth2:
    authorized-redirect-uris:
      - http://localhost:3000/oauth2/redirect
      - https://your-domain.com/oauth2/redirect
```

### 환경 변수

```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# JWT
JWT_SECRET=your-256-bit-secret-key-here-must-be-long-enough
```

---

## 7. 마이그레이션 SQL

```sql
-- 1. members 테이블 생성
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500),
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_members_provider UNIQUE (provider, provider_id)
);

-- 2. gatherings 테이블에 member_id 추가
ALTER TABLE gatherings
ADD COLUMN member_id BIGINT;

-- 3. 기존 데이터 처리 (필요 시)
-- 기존 데이터가 있다면 기본 사용자를 생성하고 연결
-- INSERT INTO members (email, nickname, provider, provider_id, role)
-- VALUES ('admin@example.com', 'Admin', 'SYSTEM', 'system-admin', 'ADMIN');
-- UPDATE gatherings SET member_id = 1 WHERE member_id IS NULL;

-- 4. member_id NOT NULL 제약조건 추가
ALTER TABLE gatherings
ALTER COLUMN member_id SET NOT NULL;

-- 5. Foreign Key 추가
ALTER TABLE gatherings
ADD CONSTRAINT fk_gatherings_member
FOREIGN KEY (member_id) REFERENCES members(id);

-- 6. 인덱스 추가
CREATE INDEX idx_gatherings_member_id ON gatherings(member_id);

-- 7. refresh_tokens 테이블 생성 (선택)
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id),
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_member_id ON refresh_tokens(member_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
```

---

## 8. 보안 고려사항

### JWT 보안

- [ ] Secret Key는 256비트 이상 사용
- [ ] Access Token은 짧은 만료 시간 (1시간)
- [ ] Refresh Token은 DB 저장 및 검증
- [ ] 로그아웃 시 Refresh Token 삭제

### OAuth2 보안

- [ ] State 파라미터로 CSRF 방지
- [ ] Redirect URI 화이트리스트 검증
- [ ] Client Secret은 환경 변수로 관리

### API 보안

- [ ] 본인 리소스만 접근 가능하도록 검증
- [ ] Rate Limiting 적용 고려
- [ ] CORS 설정 적용
