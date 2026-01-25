# N-Bang API - AWS 배포 아키텍처

## 목차

1. [전체 아키텍처](#1-전체-아키텍처)
2. [AWS 리소스 구성](#2-aws-리소스-구성)
3. [Frontend 배포](#3-frontend-배포)
4. [Backend 배포](#4-backend-배포)
5. [보안 설정](#5-보안-설정)
6. [GitHub Actions CI/CD](#6-github-actions-cicd)
7. [배포 전략 비교](#7-배포-전략-비교)
8. [월별 비용 추산](#8-월별-비용-추산)
9. [초기 설정 가이드](#9-초기-설정-가이드)
10. [모니터링 시스템](#10-모니터링-시스템-grafana--loki--alloy--prometheus)
11. [로그 관리](#11-로그-관리-mdc--rolling--구조화-로깅)

---

## 1. 전체 아키텍처

### 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                      AWS Cloud                                          │
│                                                                                         │
│  ┌───────────────────────────────────────────────────────────────────────────────────┐  │
│  │                              Frontend Flow                                         │  │
│  │                                                                                    │  │
│  │   GitHub ─────▶ GitHub Actions ─────▶ S3 ─────▶ CloudFront ◀───── Route 53       │  │
│  │   (front-live)    (Build & Deploy)   (Static)    (CDN+SSL)        (Domain)        │  │
│  │                                                                                    │  │
│  └───────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                         │
│  ┌───────────────────────────────────────────────────────────────────────────────────┐  │
│  │                              Backend Flow                                          │  │
│  │                                                                                    │  │
│  │   GitHub ──▶ GitHub Actions ──▶ S3 ──▶ CodeDeploy ──▶ EC2                         │  │
│  │   (backend-live)  (Build)     (Artifact)  (Deploy)     │                          │  │
│  │                                                        │                          │  │
│  │   ┌────────────────────────────────────────────────────┼─────────────────────┐    │  │
│  │   │                         EC2 Instance (t3.small)    │                     │    │  │
│  │   │                                                    ▼                     │    │  │
│  │   │  ┌─────────────────────────────────────────────────────────────────┐    │    │  │
│  │   │  │                      Docker Compose                              │    │    │  │
│  │   │  │  ┌─────────────────┐              ┌─────────────────┐           │    │    │  │
│  │   │  │  │     Nginx       │──────────────▶│  Spring Boot   │           │    │    │  │
│  │   │  │  │   (:80, :443)   │              │    (:8080)      │           │    │    │  │
│  │   │  │  └─────────────────┘              └─────────────────┘           │    │    │  │
│  │   │  └─────────────────────────────────────────────────────────────────┘    │    │  │
│  │   └─────────────────────────────────────────────────────────────────────────┘    │  │
│  │                                          │                                        │  │
│  └──────────────────────────────────────────┼────────────────────────────────────────┘  │
│                                             │                                           │
│  ┌──────────────────────────────────────────┼────────────────────────────────────────┐  │
│  │                              Data & Security                                       │  │
│  │                                          │                                         │  │
│  │   ┌──────────────────┐                   │         ┌──────────────────────┐       │  │
│  │   │ Secrets Manager  │◀──── Read ────────┤         │   RDS PostgreSQL     │       │  │
│  │   │ (DB Credentials) │                   └────────▶│   (db.t3.micro)      │       │  │
│  │   └──────────────────┘                             │   Private Subnet     │       │  │
│  │                                                    └──────────────────────┘       │  │
│  │                                                                                    │  │
│  └────────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### 기술 스택

| 레이어 | 기술 |
|--------|------|
| **Frontend** | React/Vue + S3 + CloudFront |
| **Backend** | Spring Boot 4.0.1 + Kotlin + Java 21 |
| **Database** | PostgreSQL 16 (RDS) |
| **Container** | Docker + Docker Compose |
| **CI/CD** | GitHub Actions + CodeDeploy |
| **보안** | AWS Secrets Manager + ACM SSL |

---

## 2. AWS 리소스 구성

### VPC & 네트워크

```
VPC (10.0.0.0/16)
├── Public Subnet (10.0.1.0/24) - AZ-a
│   └── EC2 Instance
│
├── Private Subnet (10.0.2.0/24) - AZ-a
│   └── RDS PostgreSQL (Primary)
│
└── Private Subnet (10.0.3.0/24) - AZ-c
    └── RDS PostgreSQL (Standby - Multi-AZ 사용 시)
```

### Security Groups

| 이름 | 포트 | 소스 | 설명 |
|------|------|------|------|
| **sg-ec2** | 22 | My IP | SSH 접속 |
| | 80 | 0.0.0.0/0 | HTTP |
| | 443 | 0.0.0.0/0 | HTTPS |
| **sg-rds** | 5432 | sg-ec2 | PostgreSQL (EC2에서만 접근) |

### 리소스 사양

| 서비스 | 사양 | 용도 |
|--------|------|------|
| **EC2** | t3.small (2 vCPU, 2GB RAM) | Application Server |
| **EBS** | gp3 30GB | EC2 Storage |
| **RDS** | db.t3.micro (1 vCPU, 1GB RAM) | Database |
| **RDS Storage** | gp2 20GB | Database Storage |

---

## 3. Frontend 배포

### 구성 요소

| 서비스 | 역할 | 설정 |
|--------|------|------|
| **Route 53** | DNS 관리 | Hosted Zone + A Record (Alias) |
| **ACM** | SSL 인증서 | CloudFront용 (us-east-1 리전 필수) |
| **CloudFront** | CDN | S3 Origin, HTTPS 강제, Gzip 압축 |
| **S3** | 정적 파일 호스팅 | Private bucket + OAC |

### CloudFront 설정

```yaml
Origins:
  - S3 Bucket (OAC로 접근)

Behaviors:
  - Default (*):
      ViewerProtocolPolicy: redirect-to-https
      CachePolicyId: CachingOptimized
      Compress: true
      TTL: 86400 (1일)

  - /index.html:
      CachePolicyId: CachingDisabled (또는 짧은 TTL)

ErrorPages:
  - 403 → /index.html (SPA 라우팅 지원)
  - 404 → /index.html
```

### S3 버킷 정책

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCloudFrontOAC",
      "Effect": "Allow",
      "Principal": {
        "Service": "cloudfront.amazonaws.com"
      },
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::your-frontend-bucket/*",
      "Condition": {
        "StringEquals": {
          "AWS:SourceArn": "arn:aws:cloudfront::ACCOUNT_ID:distribution/DISTRIBUTION_ID"
        }
      }
    }
  ]
}
```

---

## 4. Backend 배포

### 선택된 배포 전략: CodeDeploy In-Place

> **참고**: 배포 중 수 초간 순단이 발생할 수 있습니다. 완전한 무중단이 필요한 경우 [섹션 7](#7-배포-전략-비교)의 대안을 참고하세요.

### 프로젝트 구조

```
n-bang-api/
├── .github/
│   └── workflows/
│       └── deploy-backend.yml
├── deploy/
│   ├── appspec.yml
│   ├── docker-compose.prod.yml
│   ├── nginx/
│   │   └── nginx.conf
│   └── scripts/
│       ├── before_install.sh
│       ├── after_install.sh
│       ├── application_start.sh
│       └── application_stop.sh
├── src/
│   └── main/
│       └── resources/
│           ├── application.yml
│           └── application-prod.yml
└── Dockerfile
```

### Dockerfile

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### appspec.yml (CodeDeploy)

```yaml
version: 0.0
os: linux

files:
  - source: /
    destination: /home/ec2-user/n-bang
    overwrite: yes

permissions:
  - object: /home/ec2-user/n-bang
    owner: ec2-user
    group: ec2-user
    mode: 755
    type:
      - directory
      - file

hooks:
  BeforeInstall:
    - location: deploy/scripts/before_install.sh
      timeout: 60
      runas: root

  AfterInstall:
    - location: deploy/scripts/after_install.sh
      timeout: 120
      runas: ec2-user

  ApplicationStop:
    - location: deploy/scripts/application_stop.sh
      timeout: 60
      runas: ec2-user

  ApplicationStart:
    - location: deploy/scripts/application_start.sh
      timeout: 120
      runas: ec2-user
```

### 배포 스크립트

#### deploy/scripts/before_install.sh

```bash
#!/bin/bash
set -e

# 배포 디렉토리 정리
if [ -d /home/ec2-user/n-bang ]; then
    rm -rf /home/ec2-user/n-bang/*
fi

# 디렉토리 생성
mkdir -p /home/ec2-user/n-bang
chown -R ec2-user:ec2-user /home/ec2-user/n-bang
```

#### deploy/scripts/after_install.sh

```bash
#!/bin/bash
set -e

cd /home/ec2-user/n-bang

# Docker 이미지 빌드
docker build -t n-bang-api:latest .

# 스크립트 실행 권한 부여
chmod +x deploy/scripts/*.sh
```

#### deploy/scripts/application_stop.sh

```bash
#!/bin/bash

cd /home/ec2-user/n-bang

# 기존 컨테이너 중지 (존재하는 경우)
if [ -f deploy/docker-compose.prod.yml ]; then
    docker compose -f deploy/docker-compose.prod.yml down || true
fi

echo "Application stopped"
```

#### deploy/scripts/application_start.sh

```bash
#!/bin/bash
set -e

cd /home/ec2-user/n-bang

# 환경 변수 설정
export AWS_REGION=ap-northeast-2

# Docker Compose로 애플리케이션 시작
docker compose -f deploy/docker-compose.prod.yml up -d

# Health Check (최대 60초 대기)
echo "Waiting for application to start..."
for i in {1..12}; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "Application started successfully!"
        exit 0
    fi
    echo "Waiting... ($i/12)"
    sleep 5
done

echo "Application failed to start!"
exit 1
```

### docker-compose.prod.yml

```yaml
services:
  app:
    image: n-bang-api:latest
    container_name: n-bang-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - AWS_REGION=ap-northeast-2
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 40s
    restart: unless-stopped
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  nginx:
    image: nginx:alpine
    container_name: n-bang-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./deploy/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    depends_on:
      - app
    restart: unless-stopped
```

### nginx.conf

```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server host.docker.internal:8080;
    }

    # HTTP → HTTPS 리다이렉트
    server {
        listen 80;
        server_name api.your-domain.com;
        return 301 https://$server_name$request_uri;
    }

    # HTTPS 서버
    server {
        listen 443 ssl http2;
        server_name api.your-domain.com;

        # SSL 인증서 (Let's Encrypt)
        ssl_certificate /etc/letsencrypt/live/api.your-domain.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/api.your-domain.com/privkey.pem;

        # SSL 설정
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
        ssl_prefer_server_ciphers off;
        ssl_session_cache shared:SSL:10m;
        ssl_session_timeout 1d;

        # 보안 헤더
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;

        # 프록시 설정
        location / {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # Health Check (로깅 제외)
        location /actuator/health {
            proxy_pass http://backend;
            access_log off;
        }
    }
}
```

---

## 5. 보안 설정

### AWS Secrets Manager

#### 시크릿 구조

```json
{
  "secretName": "n-bang/prod/database",
  "secretValue": {
    "host": "n-bang-db.xxxxx.ap-northeast-2.rds.amazonaws.com",
    "port": "5432",
    "database": "nbang",
    "username": "nbang_admin",
    "password": "your-secure-password"
  }
}
```

#### 시크릿 생성 (AWS CLI)

```bash
aws secretsmanager create-secret \
  --name n-bang/prod/database \
  --secret-string '{
    "host": "your-rds-endpoint",
    "port": "5432",
    "database": "nbang",
    "username": "nbang_admin",
    "password": "your-secure-password"
  }'
```

### Spring Boot 설정

#### build.gradle.kts (의존성 추가)

```kotlin
dependencies {
    // 기존 의존성...

    // AWS Secrets Manager
    implementation("io.awspring.cloud:spring-cloud-aws-starter-secrets-manager:3.1.0")

    // Health Check
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
```

#### application-prod.yml

```yaml
spring:
  application:
    name: n-bang-api

  config:
    import: optional:aws-secretsmanager:n-bang/prod/database

  datasource:
    url: jdbc:postgresql://${host}:${port}/${database}
    username: ${username}
    password: ${password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
  shutdown: graceful

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    com.nbang: INFO
```

### IAM 정책

#### EC2 Instance Role

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "SecretsManagerAccess",
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "arn:aws:secretsmanager:ap-northeast-2:*:secret:n-bang/prod/*"
    },
    {
      "Sid": "CodeDeployS3Access",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:GetObjectVersion"
      ],
      "Resource": "arn:aws:s3:::your-codedeploy-bucket/*"
    }
  ]
}
```

#### GitHub Actions IAM User

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3DeployAccess",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-frontend-bucket",
        "arn:aws:s3:::your-frontend-bucket/*",
        "arn:aws:s3:::your-codedeploy-bucket",
        "arn:aws:s3:::your-codedeploy-bucket/*"
      ]
    },
    {
      "Sid": "CloudFrontAccess",
      "Effect": "Allow",
      "Action": [
        "cloudfront:CreateInvalidation"
      ],
      "Resource": "*"
    },
    {
      "Sid": "CodeDeployAccess",
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetDeployment",
        "codedeploy:GetDeploymentConfig",
        "codedeploy:GetApplicationRevision",
        "codedeploy:RegisterApplicationRevision"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## 6. GitHub Actions CI/CD

### GitHub Secrets 설정

```
Repository Settings > Secrets and variables > Actions

┌─────────────────────────────────────────────────────────┐
│                   Required Secrets                      │
├─────────────────────────────────────────────────────────┤
│  AWS_ACCESS_KEY_ID          │ AKIA...                   │
│  AWS_SECRET_ACCESS_KEY      │ xxxxx...                  │
│  AWS_REGION                 │ ap-northeast-2            │
│  S3_BUCKET_FRONTEND         │ your-frontend-bucket      │
│  S3_BUCKET_CODEDEPLOY       │ your-codedeploy-bucket    │
│  CLOUDFRONT_DISTRIBUTION_ID │ EXXXXXXXXXX               │
│  CODEDEPLOY_APP_NAME        │ n-bang-api                │
│  CODEDEPLOY_GROUP_NAME      │ n-bang-api-group          │
└─────────────────────────────────────────────────────────┘
```

### Backend: deploy-backend.yml

```yaml
name: Deploy Backend

on:
  push:
    branches:
      - backend-live

env:
  AWS_REGION: ap-northeast-2

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Run tests
        run: ./gradlew test --no-daemon

  deploy:
    needs: test
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Create deployment package
        run: |
          zip -r deploy.zip . \
            -x "*.git*" \
            -x "*.idea*" \
            -x "*.gradle*" \
            -x "build/*" \
            -x "*.md"

      - name: Upload to S3
        run: |
          aws s3 cp deploy.zip s3://${{ secrets.S3_BUCKET_CODEDEPLOY }}/n-bang-api/${{ github.sha }}.zip

      - name: Create CodeDeploy deployment
        run: |
          aws deploy create-deployment \
            --application-name ${{ secrets.CODEDEPLOY_APP_NAME }} \
            --deployment-group-name ${{ secrets.CODEDEPLOY_GROUP_NAME }} \
            --deployment-config-name CodeDeployDefault.AllAtOnce \
            --s3-location bucket=${{ secrets.S3_BUCKET_CODEDEPLOY }},key=n-bang-api/${{ github.sha }}.zip,bundleType=zip \
            --description "Deployment from GitHub Actions - ${{ github.sha }}"
```

### Frontend: deploy-frontend.yml

```yaml
name: Deploy Frontend

on:
  push:
    branches:
      - front-live

env:
  AWS_REGION: ap-northeast-2

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Build
        run: npm run build
        env:
          VITE_API_URL: https://api.your-domain.com

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Deploy to S3
        run: |
          # 정적 파일 (긴 캐시)
          aws s3 sync dist/ s3://${{ secrets.S3_BUCKET_FRONTEND }} \
            --delete \
            --cache-control "public, max-age=31536000, immutable" \
            --exclude "index.html" \
            --exclude "*.json"

          # index.html (캐시 없음)
          aws s3 cp dist/index.html s3://${{ secrets.S3_BUCKET_FRONTEND }}/index.html \
            --cache-control "no-cache, no-store, must-revalidate"

      - name: Invalidate CloudFront cache
        run: |
          aws cloudfront create-invalidation \
            --distribution-id ${{ secrets.CLOUDFRONT_DISTRIBUTION_ID }} \
            --paths "/index.html"
```

---

## 7. 배포 전략 비교

### 전략별 비교표

| 항목 | CodeDeploy In-Place | Nginx Blue-Green | CodeDeploy Blue-Green |
|------|---------------------|------------------|----------------------|
| **추가 비용** | $0 | $0 | +$18~22/월 (ALB) |
| **무중단 보장** | △ (순단 발생) | ○ | ◎ |
| **롤백** | 자동 | 수동 스크립트 | 자동 |
| **복잡도** | 중간 | 낮음 | 높음 |
| **단일 EC2** | ○ | ○ | ✗ (ASG 필요) |
| **권장 상황** | 순단 허용 가능 | 비용 최소화 | 프로덕션 서비스 |

---

### 대안 1: Nginx Blue-Green (단일 EC2, 무중단)

단일 EC2 내에서 두 개의 Docker 컨테이너를 운영하여 무중단 배포를 구현합니다.

#### 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    EC2 Instance                         │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │                    Nginx                         │   │
│  │              (트래픽 전환 담당)                   │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                  │
│        ┌─────────────┴─────────────┐                   │
│        ▼                           ▼                   │
│  ┌───────────┐              ┌───────────┐              │
│  │   Blue    │              │   Green   │              │
│  │  (:8080)  │              │  (:8081)  │              │
│  │  Active   │              │  Standby  │              │
│  └───────────┘              └───────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

#### docker-compose.blue-green.yml

```yaml
services:
  app-blue:
    image: ${ECR_REGISTRY}/n-bang-api:${IMAGE_TAG}
    container_name: n-bang-blue
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - AWS_REGION=ap-northeast-2
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 40s
    restart: unless-stopped

  app-green:
    image: ${ECR_REGISTRY}/n-bang-api:${IMAGE_TAG}
    container_name: n-bang-green
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - AWS_REGION=ap-northeast-2
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 40s
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: n-bang-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    depends_on:
      - app-blue
    restart: unless-stopped
```

#### Blue-Green 배포 스크립트 (deploy-blue-green.sh)

```bash
#!/bin/bash
set -e

ECR_REGISTRY=$1
IMAGE_TAG=$2
DEPLOY_DIR="/home/ec2-user/n-bang"

cd $DEPLOY_DIR

# 현재 활성 컨테이너 확인
CURRENT_ACTIVE=$(docker ps --format '{{.Names}}' | grep -E 'n-bang-(blue|green)' | head -1 || echo "none")

if [ "$CURRENT_ACTIVE" == "n-bang-blue" ] || [ "$CURRENT_ACTIVE" == "none" ]; then
    NEW_CONTAINER="green"
    NEW_PORT="8081"
    OLD_CONTAINER="blue"
    OLD_PORT="8080"
else
    NEW_CONTAINER="blue"
    NEW_PORT="8080"
    OLD_CONTAINER="green"
    OLD_PORT="8081"
fi

echo ">>> Current: $OLD_CONTAINER, Deploying to: $NEW_CONTAINER"

# 환경 변수 설정
export ECR_REGISTRY=$ECR_REGISTRY
export IMAGE_TAG=$IMAGE_TAG

# 새 이미지 Pull
echo ">>> Pulling new image..."
docker pull ${ECR_REGISTRY}/n-bang-api:${IMAGE_TAG}

# 새 컨테이너 시작
echo ">>> Starting $NEW_CONTAINER container..."
docker compose -f docker-compose.blue-green.yml up -d app-$NEW_CONTAINER

# Health Check (최대 60초 대기)
echo ">>> Waiting for health check..."
for i in {1..12}; do
    if curl -sf http://localhost:$NEW_PORT/actuator/health > /dev/null 2>&1; then
        echo ">>> Health check passed!"
        break
    fi
    if [ $i -eq 12 ]; then
        echo ">>> Health check failed! Rolling back..."
        docker compose -f docker-compose.blue-green.yml stop app-$NEW_CONTAINER
        exit 1
    fi
    echo ">>> Waiting... ($i/12)"
    sleep 5
done

# Nginx 설정 업데이트 (트래픽 전환)
echo ">>> Switching traffic to $NEW_CONTAINER..."
sed -i "s/host.docker.internal:$OLD_PORT/host.docker.internal:$NEW_PORT/g" nginx/nginx.conf
docker exec n-bang-nginx nginx -s reload

# 이전 컨테이너 정지 (graceful)
echo ">>> Stopping $OLD_CONTAINER container..."
sleep 10  # 기존 요청 처리 대기
docker compose -f docker-compose.blue-green.yml stop app-$OLD_CONTAINER

# 오래된 이미지 정리
echo ">>> Cleaning up old images..."
docker image prune -f

echo ">>> Deployment completed! Active: $NEW_CONTAINER"
```

---

### 대안 2: CodeDeploy Blue-Green (ALB 사용)

완전한 무중단 배포와 자동 롤백을 원할 경우 ALB와 Auto Scaling Group을 사용합니다.

#### 아키텍처

```
┌────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                              │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                    Application Load Balancer              │ │
│  │                      (약 $18/월 추가)                     │ │
│  └──────────────────────────┬───────────────────────────────┘ │
│                             │                                  │
│           ┌─────────────────┴─────────────────┐               │
│           ▼                                   ▼               │
│  ┌─────────────────────┐            ┌─────────────────────┐  │
│  │   Target Group      │            │   Target Group      │  │
│  │       Blue          │            │       Green         │  │
│  │  ┌───────────────┐  │            │  ┌───────────────┐  │  │
│  │  │ ASG (EC2 * N) │  │            │  │ ASG (EC2 * N) │  │  │
│  │  └───────────────┘  │            │  └───────────────┘  │  │
│  └─────────────────────┘            └─────────────────────┘  │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

#### CodeDeploy 설정

```yaml
# appspec.yml for Blue-Green
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "arn:aws:ecs:ap-northeast-2:ACCOUNT:task-definition/n-bang-api"
        LoadBalancerInfo:
          ContainerName: "n-bang-api"
          ContainerPort: 8080
```

#### GitHub Actions (Blue-Green용)

```yaml
- name: Create CodeDeploy Blue-Green deployment
  run: |
    aws deploy create-deployment \
      --application-name ${{ secrets.CODEDEPLOY_APP_NAME }} \
      --deployment-group-name ${{ secrets.CODEDEPLOY_GROUP_NAME }} \
      --deployment-config-name CodeDeployDefault.ECSAllAtOnce \
      --s3-location bucket=${{ secrets.S3_BUCKET_CODEDEPLOY }},key=n-bang-api/${{ github.sha }}.zip,bundleType=zip \
      --file-exists-behavior OVERWRITE
```

---

## 8. 월별 비용 추산

### 선택된 구성 (CodeDeploy In-Place)

| 서비스 | 사양 | 월 비용 (USD) |
|--------|------|---------------|
| **EC2** | t3.small (2 vCPU, 2GB) | ~$17.00 |
| **EBS** | gp3 30GB | ~$2.40 |
| **RDS PostgreSQL** | db.t3.micro (1 vCPU, 1GB) | ~$15.00 |
| **RDS Storage** | gp2 20GB | ~$2.30 |
| **S3** | Frontend 1GB + Artifacts | ~$0.50 |
| **CloudFront** | 10GB 전송/월 | ~$0.85 |
| **Secrets Manager** | 1개 시크릿 | ~$0.40 |
| **Route 53** | Hosted Zone 1개 | $0.50 |
| **CodeDeploy** | - | 무료 |
| **Elastic IP** | 1개 (사용중) | $0 |
| **Data Transfer** | EC2 → Internet 10GB | ~$0.90 |
| **도메인** | .com (연간 ~$12) | ~$1.00 |
| | | |
| **합계** | | **~$41/월** |

### 대안별 비용

| 구성 | 월 비용 |
|------|---------|
| CodeDeploy In-Place (선택됨) | ~$41/월 |
| Nginx Blue-Green | ~$41/월 (동일) |
| CodeDeploy Blue-Green + ALB | ~$59/월 (+$18) |

---

## 9. 초기 설정 가이드

### 설정 순서 체크리스트

```
AWS 인프라 설정
────────────────
[ ] 1. VPC 생성 (10.0.0.0/16)
[ ] 2. 서브넷 생성 (Public: 10.0.1.0/24, Private: 10.0.2.0/24, 10.0.3.0/24)
[ ] 3. Internet Gateway 생성 및 연결
[ ] 4. Route Table 설정
[ ] 5. Security Groups 생성 (sg-ec2, sg-rds)

데이터베이스 설정
────────────────
[ ] 6. RDS Subnet Group 생성
[ ] 7. RDS PostgreSQL 인스턴스 생성 (db.t3.micro)
[ ] 8. Secrets Manager에 DB 자격증명 저장

EC2 설정
────────────────
[ ] 9. IAM Role 생성 (SecretsManager + CodeDeploy 접근)
[ ] 10. EC2 인스턴스 생성 (Amazon Linux 2023, t3.small)
[ ] 11. Elastic IP 할당 및 연결
[ ] 12. Docker, Docker Compose 설치
[ ] 13. CodeDeploy Agent 설치
[ ] 14. Let's Encrypt SSL 인증서 발급

CodeDeploy 설정
────────────────
[ ] 15. CodeDeploy Application 생성
[ ] 16. Deployment Group 생성
[ ] 17. S3 버킷 생성 (배포 아티팩트용)

Frontend 설정
────────────────
[ ] 18. S3 버킷 생성 (정적 호스팅용)
[ ] 19. ACM 인증서 발급 (us-east-1)
[ ] 20. CloudFront 배포 생성
[ ] 21. S3 버킷 정책 설정 (OAC)

DNS 설정
────────────────
[ ] 22. Route 53 Hosted Zone 생성
[ ] 23. A Record 설정 (Frontend → CloudFront)
[ ] 24. A Record 설정 (API → EC2 Elastic IP)

CI/CD 설정
────────────────
[ ] 25. IAM User 생성 (GitHub Actions용)
[ ] 26. GitHub Secrets 설정
[ ] 27. GitHub Actions 워크플로우 파일 커밋
[ ] 28. 브랜치 생성 (front-live, backend-live)
```

### EC2 초기 설정 스크립트

```bash
#!/bin/bash
# EC2 인스턴스 초기 설정 (Amazon Linux 2023)

# 시스템 업데이트
sudo dnf update -y

# Docker 설치
sudo dnf install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# CodeDeploy Agent 설치
sudo dnf install -y ruby wget
cd /home/ec2-user
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
sudo ./install auto
sudo systemctl start codedeploy-agent
sudo systemctl enable codedeploy-agent

# Let's Encrypt (Certbot) 설치
sudo dnf install -y certbot

# 인증서 발급 (도메인 설정 후 실행)
# sudo certbot certonly --standalone -d api.your-domain.com

# 배포 디렉토리 생성
mkdir -p /home/ec2-user/n-bang
```

### 배포 테스트

```bash
# Backend 배포 트리거
git checkout -b backend-live
git push origin backend-live

# Frontend 배포 트리거
git checkout -b front-live
git push origin front-live

# 배포 상태 확인
aws deploy get-deployment --deployment-id d-XXXXXXXXX
```

---

## 부록: 문제 해결

### 자주 발생하는 문제

| 문제 | 원인 | 해결 방법 |
|------|------|-----------|
| CodeDeploy 타임아웃 | 스크립트 실행 시간 초과 | appspec.yml의 timeout 값 증가 |
| Health Check 실패 | 앱 시작 지연 | start_period 증가, JVM 워밍업 추가 |
| Secrets Manager 접근 불가 | IAM Role 미설정 | EC2 Instance Profile 확인 |
| Docker 권한 오류 | ec2-user 그룹 미추가 | `sudo usermod -aG docker ec2-user` 후 재로그인 |
| RDS 연결 실패 | Security Group 설정 | sg-rds가 sg-ec2를 허용하는지 확인 |

### 로그 확인

```bash
# CodeDeploy Agent 로그
tail -f /var/log/aws/codedeploy-agent/codedeploy-agent.log

# 배포 스크립트 로그
tail -f /opt/codedeploy-agent/deployment-root/deployment-logs/codedeploy-agent-deployments.log

# Docker 로그
docker logs -f n-bang-app

# Nginx 로그
docker logs -f n-bang-nginx
```

---

## 10. 모니터링 시스템 (Grafana + Loki + Alloy + Prometheus)

### 모니터링 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                 EC2 Instance                                            │
│                                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                              Application Layer                                   │   │
│  │                                                                                  │   │
│  │   ┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐   │   │
│  │   │     Nginx       │────────▶│  Spring Boot    │────────▶│   PostgreSQL    │   │   │
│  │   │   (:80, :443)   │         │    (:8080)      │         │   (RDS)         │   │   │
│  │   └─────────────────┘         └────────┬────────┘         └─────────────────┘   │   │
│  │                                        │                                         │   │
│  │                          /actuator/prometheus                                    │   │
│  │                                        │                                         │   │
│  └────────────────────────────────────────┼─────────────────────────────────────────┘   │
│                                           │                                             │
│  ┌────────────────────────────────────────┼─────────────────────────────────────────┐   │
│  │                           Monitoring Layer                                        │   │
│  │                                        │                                          │   │
│  │                                        ▼                                          │   │
│  │   ┌─────────────────────────────────────────────────────────────────────────┐    │   │
│  │   │                         Grafana Alloy                                    │    │   │
│  │   │                      (Collector Agent)                                   │    │   │
│  │   │                                                                          │    │   │
│  │   │   ┌─────────────────────┐       ┌─────────────────────┐                 │    │   │
│  │   │   │   Metrics Scrape    │       │    Log Collection   │                 │    │   │
│  │   │   │ (Prometheus Format) │       │   (Docker Logs)     │                 │    │   │
│  │   │   └──────────┬──────────┘       └──────────┬──────────┘                 │    │   │
│  │   │              │                             │                             │    │   │
│  │   └──────────────┼─────────────────────────────┼─────────────────────────────┘    │   │
│  │                  │                             │                                  │   │
│  │                  ▼                             ▼                                  │   │
│  │   ┌─────────────────────┐       ┌─────────────────────┐                          │   │
│  │   │     Prometheus      │       │        Loki         │                          │   │
│  │   │      (:9090)        │       │      (:3100)        │                          │   │
│  │   │  (Metrics Storage)  │       │   (Log Storage)     │                          │   │
│  │   └──────────┬──────────┘       └──────────┬──────────┘                          │   │
│  │              │                             │                                      │   │
│  │              └──────────────┬──────────────┘                                      │   │
│  │                             │                                                     │   │
│  │                             ▼                                                     │   │
│  │              ┌─────────────────────────────┐                                      │   │
│  │              │          Grafana            │                                      │   │
│  │              │          (:3000)            │                                      │   │
│  │              │     (Visualization)         │◀──── 사용자 접속                     │   │
│  │              └─────────────────────────────┘                                      │   │
│  │                                                                                   │   │
│  └───────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### 데이터 흐름

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   Data Flow                                             │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│   Metrics Flow (메트릭 수집)                                                             │
│   ──────────────────────────                                                            │
│                                                                                         │
│   Spring Boot ──▶ /actuator/prometheus ──▶ Alloy ──▶ Prometheus ──▶ Grafana           │
│       │                                                                                 │
│       └── Micrometer가 메트릭 생성 (JVM, HTTP, DB Connection Pool 등)                   │
│                                                                                         │
│                                                                                         │
│   Logs Flow (로그 수집)                                                                  │
│   ─────────────────────                                                                 │
│                                                                                         │
│   Docker Container ──▶ /var/lib/docker/containers/*.log ──▶ Alloy ──▶ Loki ──▶ Grafana │
│       │                                                                                 │
│       └── JSON 형식 로그 → 파싱 → 라벨 추가 → Loki 저장                                  │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### 프로젝트 구조 (모니터링 추가)

```
n-bang-api/
├── deploy/
│   ├── docker-compose.prod.yml
│   ├── docker-compose.monitoring.yml    # 모니터링 스택
│   ├── monitoring/
│   │   ├── alloy/
│   │   │   └── config.alloy             # Alloy 설정
│   │   ├── prometheus/
│   │   │   └── prometheus.yml           # Prometheus 설정
│   │   ├── loki/
│   │   │   └── loki-config.yml          # Loki 설정
│   │   └── grafana/
│   │       ├── provisioning/
│   │       │   ├── datasources/
│   │       │   │   └── datasources.yml  # 데이터소스 자동 설정
│   │       │   └── dashboards/
│   │       │       ├── dashboards.yml
│   │       │       └── spring-boot.json # 대시보드 템플릿
│   │       └── grafana.ini              # Grafana 설정
│   └── nginx/
│       └── nginx.conf
└── src/
    └── main/
        └── resources/
            └── application-prod.yml
```

---

### Spring Boot 설정 (메트릭 노출)

#### build.gradle.kts (의존성 추가)

```kotlin
dependencies {
    // 기존 의존성...

    // Actuator + Micrometer (Prometheus)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

#### application-prod.yml (메트릭 엔드포인트 설정)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    tags:
      application: n-bang-api
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

---

### Docker Compose (모니터링 스택)

#### docker-compose.monitoring.yml

```yaml
services:
  # ============================================
  # Grafana Alloy (Collector)
  # ============================================
  alloy:
    image: grafana/alloy:latest
    container_name: n-bang-alloy
    volumes:
      - ./monitoring/alloy/config.alloy:/etc/alloy/config.alloy:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    command:
      - run
      - /etc/alloy/config.alloy
      - --storage.path=/var/lib/alloy/data
      - --server.http.listen-addr=0.0.0.0:12345
    ports:
      - "12345:12345"
    depends_on:
      - prometheus
      - loki
    restart: unless-stopped
    networks:
      - monitoring

  # ============================================
  # Prometheus (Metrics Storage)
  # ============================================
  prometheus:
    image: prom/prometheus:latest
    container_name: n-bang-prometheus
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    ports:
      - "9090:9090"
    restart: unless-stopped
    networks:
      - monitoring

  # ============================================
  # Loki (Log Storage)
  # ============================================
  loki:
    image: grafana/loki:latest
    container_name: n-bang-loki
    volumes:
      - ./monitoring/loki/loki-config.yml:/etc/loki/loki-config.yml:ro
      - loki_data:/loki
    command: -config.file=/etc/loki/loki-config.yml
    ports:
      - "3100:3100"
    restart: unless-stopped
    networks:
      - monitoring

  # ============================================
  # Grafana (Visualization)
  # ============================================
  grafana:
    image: grafana/grafana:latest
    container_name: n-bang-grafana
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SERVER_ROOT_URL=https://grafana.your-domain.com
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
      - ./monitoring/grafana/grafana.ini:/etc/grafana/grafana.ini:ro
      - grafana_data:/var/lib/grafana
    ports:
      - "3000:3000"
    depends_on:
      - prometheus
      - loki
    restart: unless-stopped
    networks:
      - monitoring

networks:
  monitoring:
    driver: bridge

volumes:
  prometheus_data:
  loki_data:
  grafana_data:
```

---

### Alloy 설정

#### monitoring/alloy/config.alloy

```hcl
// ============================================
// Prometheus Metrics Scraping
// ============================================

prometheus.scrape "spring_boot" {
  targets = [
    {"__address__" = "host.docker.internal:8080", "job" = "spring-boot", "app" = "n-bang-api"},
  ]
  forward_to = [prometheus.remote_write.default.receiver]
  scrape_interval = "15s"
  metrics_path = "/actuator/prometheus"
}

prometheus.scrape "nginx" {
  targets = [
    {"__address__" = "host.docker.internal:9113", "job" = "nginx"},
  ]
  forward_to = [prometheus.remote_write.default.receiver]
  scrape_interval = "15s"
  enabled = false  // nginx-prometheus-exporter 사용 시 활성화
}

prometheus.remote_write "default" {
  endpoint {
    url = "http://prometheus:9090/api/v1/write"
  }
}

// ============================================
// Docker Log Collection
// ============================================

discovery.docker "containers" {
  host = "unix:///var/run/docker.sock"
  refresh_interval = "5s"
}

discovery.relabel "containers" {
  targets = discovery.docker.containers.targets

  // 컨테이너 이름으로 필터링
  rule {
    source_labels = ["__meta_docker_container_name"]
    regex = "/(n-bang-app|n-bang-nginx)"
    action = "keep"
  }

  // 라벨 설정
  rule {
    source_labels = ["__meta_docker_container_name"]
    target_label = "container"
    regex = "/(.*)"
    replacement = "$1"
  }

  rule {
    source_labels = ["__meta_docker_container_log_stream"]
    target_label = "stream"
  }
}

loki.source.docker "containers" {
  host = "unix:///var/run/docker.sock"
  targets = discovery.relabel.containers.output
  forward_to = [loki.process.containers.receiver]
  refresh_interval = "5s"
}

loki.process "containers" {
  forward_to = [loki.write.default.receiver]

  // JSON 로그 파싱 (Spring Boot)
  stage.json {
    expressions = {
      level = "level",
      logger = "logger_name",
      message = "message",
      timestamp = "@timestamp",
    }
  }

  // 라벨 추가
  stage.labels {
    values = {
      level = "",
      logger = "",
    }
  }

  // 타임스탬프 설정
  stage.timestamp {
    source = "timestamp"
    format = "2006-01-02T15:04:05.000Z"
  }
}

loki.write "default" {
  endpoint {
    url = "http://loki:3100/loki/api/v1/push"
  }
}
```

---

### Prometheus 설정

#### monitoring/prometheus/prometheus.yml

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  # Spring Boot 메트릭
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
        labels:
          app: 'n-bang-api'
          env: 'prod'

  # Prometheus 자체 메트릭
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Loki 메트릭
  - job_name: 'loki'
    static_configs:
      - targets: ['loki:3100']

  # Alloy 메트릭
  - job_name: 'alloy'
    static_configs:
      - targets: ['alloy:12345']
```

---

### Loki 설정

#### monitoring/loki/loki-config.yml

```yaml
auth_enabled: false

server:
  http_listen_port: 3100
  grpc_listen_port: 9096

common:
  instance_addr: 127.0.0.1
  path_prefix: /loki
  storage:
    filesystem:
      chunks_directory: /loki/chunks
      rules_directory: /loki/rules
  replication_factor: 1
  ring:
    kvstore:
      store: inmemory

query_range:
  results_cache:
    cache:
      embedded_cache:
        enabled: true
        max_size_mb: 100

schema_config:
  configs:
    - from: 2020-10-24
      store: tsdb
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

storage_config:
  filesystem:
    directory: /loki/chunks

limits_config:
  retention_period: 168h  # 7일 보관
  ingestion_rate_mb: 4
  ingestion_burst_size_mb: 6

compactor:
  working_directory: /loki/compactor
  compaction_interval: 10m
  retention_enabled: true
  retention_delete_delay: 2h
```

---

### Grafana 설정

#### monitoring/grafana/provisioning/datasources/datasources.yml

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    editable: false
    jsonData:
      maxLines: 1000
```

#### monitoring/grafana/provisioning/dashboards/dashboards.yml

```yaml
apiVersion: 1

providers:
  - name: 'default'
    orgId: 1
    folder: ''
    folderUid: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 30
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards
```

#### monitoring/grafana/grafana.ini

```ini
[server]
root_url = https://grafana.your-domain.com
serve_from_sub_path = false

[security]
admin_user = admin
admin_password = ${GRAFANA_PASSWORD}
disable_gravatar = true

[users]
allow_sign_up = false
allow_org_create = false

[auth.anonymous]
enabled = false

[alerting]
enabled = true

[unified_alerting]
enabled = true
```

---

### Nginx 설정 (Grafana 프록시 추가)

#### nginx.conf (수정)

```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server host.docker.internal:8080;
    }

    upstream grafana {
        server host.docker.internal:3000;
    }

    # API 서버 (기존)
    server {
        listen 443 ssl http2;
        server_name api.your-domain.com;

        ssl_certificate /etc/letsencrypt/live/api.your-domain.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/api.your-domain.com/privkey.pem;

        location / {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Actuator 엔드포인트 (내부 전용)
        location /actuator {
            # Prometheus/Alloy에서만 접근 허용
            allow 127.0.0.1;
            allow 172.16.0.0/12;  # Docker network
            deny all;
            proxy_pass http://backend;
        }
    }

    # Grafana 대시보드
    server {
        listen 443 ssl http2;
        server_name grafana.your-domain.com;

        ssl_certificate /etc/letsencrypt/live/grafana.your-domain.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/grafana.your-domain.com/privkey.pem;

        location / {
            proxy_pass http://grafana;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;

            # WebSocket 지원 (Live 기능)
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }
    }
}
```

---

### 모니터링 시작/중지 스크립트

#### deploy/scripts/monitoring_start.sh

```bash
#!/bin/bash
set -e

cd /home/ec2-user/n-bang

# 환경 변수 설정
export GRAFANA_PASSWORD=${GRAFANA_PASSWORD:-"your-secure-password"}

# 모니터링 스택 시작
docker compose -f deploy/docker-compose.monitoring.yml up -d

echo "Monitoring stack started!"
echo "Grafana: https://grafana.your-domain.com"
echo "Prometheus: http://localhost:9090 (internal)"
echo "Loki: http://localhost:3100 (internal)"
```

#### deploy/scripts/monitoring_stop.sh

```bash
#!/bin/bash

cd /home/ec2-user/n-bang

# 모니터링 스택 중지
docker compose -f deploy/docker-compose.monitoring.yml down

echo "Monitoring stack stopped!"
```

---

### 주요 메트릭 및 알림

#### 수집되는 메트릭

| 카테고리 | 메트릭 | 설명 |
|----------|--------|------|
| **JVM** | `jvm_memory_used_bytes` | 메모리 사용량 |
| | `jvm_gc_pause_seconds` | GC 일시 정지 시간 |
| | `jvm_threads_live_threads` | 활성 스레드 수 |
| **HTTP** | `http_server_requests_seconds` | 요청 응답 시간 |
| | `http_server_requests_seconds_count` | 요청 수 |
| **DB** | `hikaricp_connections_active` | 활성 DB 연결 수 |
| | `hikaricp_connections_pending` | 대기 중인 연결 수 |
| **System** | `process_cpu_usage` | CPU 사용률 |
| | `system_cpu_usage` | 시스템 CPU 사용률 |

#### Grafana 알림 규칙 예시

```yaml
# grafana/provisioning/alerting/alerts.yml
groups:
  - name: n-bang-alerts
    rules:
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum(rate(http_server_requests_seconds_count[5m])) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% for 5 minutes"

      - alert: HighResponseTime
        expr: |
          histogram_quantile(0.95,
            sum(rate(http_server_requests_seconds_bucket[5m])) by (le)
          ) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time"
          description: "95th percentile response time is above 2 seconds"

      - alert: HighMemoryUsage
        expr: |
          jvm_memory_used_bytes{area="heap"}
          / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High heap memory usage"
          description: "Heap memory usage is above 90%"
```

---

### Security Group 업데이트

모니터링 서비스를 위한 포트 추가:

| 포트 | 서비스 | 접근 |
|------|--------|------|
| 3000 | Grafana | 0.0.0.0/0 (또는 My IP) |
| 9090 | Prometheus | 127.0.0.1 (내부 전용) |
| 3100 | Loki | 127.0.0.1 (내부 전용) |
| 12345 | Alloy | 127.0.0.1 (내부 전용) |

---

### 비용 영향

#### EC2 인스턴스 업그레이드 권장

모니터링 스택 추가 시 메모리 사용량이 증가합니다:

| 구성 요소 | 예상 메모리 |
|-----------|-------------|
| Spring Boot | ~512MB |
| Nginx | ~50MB |
| Prometheus | ~200MB |
| Loki | ~200MB |
| Alloy | ~100MB |
| Grafana | ~150MB |
| **합계** | **~1.2GB** |

**권장 사항**: t3.small (2GB) → **t3.medium (4GB)** 업그레이드

#### 업데이트된 월별 비용

| 서비스 | 사양 | 월 비용 (USD) |
|--------|------|---------------|
| **EC2** | t3.medium (2 vCPU, 4GB) | ~$34.00 |
| **EBS** | gp3 50GB (로그/메트릭 저장) | ~$4.00 |
| **RDS PostgreSQL** | db.t3.micro | ~$15.00 |
| **RDS Storage** | gp2 20GB | ~$2.30 |
| **S3 + CloudFront** | 동일 | ~$1.35 |
| **기타** | Route 53, Secrets Manager 등 | ~$2.40 |
| **도메인** | .com | ~$1.00 |
| | | |
| **합계** | | **~$60/월** |

> 기존 대비 **+$19/월** 증가 (EC2 업그레이드 + EBS 증가)

---

### 초기 설정 체크리스트 (모니터링)

```
모니터링 설정
────────────────
[ ] 1. EC2 인스턴스 t3.medium으로 업그레이드
[ ] 2. EBS 볼륨 50GB로 확장
[ ] 3. Security Group에 Grafana 포트(3000) 추가
[ ] 4. grafana.your-domain.com 도메인 설정
[ ] 5. Let's Encrypt 인증서 발급 (grafana 서브도메인)
[ ] 6. monitoring 디렉토리 및 설정 파일 생성
[ ] 7. docker-compose.monitoring.yml 배포
[ ] 8. Grafana 대시보드 구성
[ ] 9. 알림 규칙 설정 (Slack/Email 연동)
```

---

### 유용한 Grafana 대시보드

미리 만들어진 대시보드를 import하여 사용할 수 있습니다:

| 대시보드 | Grafana ID | 용도 |
|----------|------------|------|
| Spring Boot Statistics | 12900 | JVM, HTTP, HikariCP 메트릭 |
| JVM Micrometer | 4701 | 상세 JVM 메트릭 |
| Loki Dashboard | 13639 | 로그 분석 |
| Docker Container | 893 | 컨테이너 리소스 |

```bash
# Grafana에서 Import
Dashboard → Import → Dashboard ID 입력 → Load
```

---

### 로그 조회 (LogQL 예시)

Grafana Explore에서 Loki 로그를 조회하는 쿼리 예시:

```logql
# 에러 로그 조회
{container="n-bang-app"} |= "ERROR"

# 특정 API 응답 시간 조회
{container="n-bang-app"} | json | line_format "{{.message}}" |= "/api/events"

# 최근 5분간 에러 수 집계
count_over_time({container="n-bang-app"} |= "ERROR" [5m])

# 로그 레벨별 집계
sum by (level) (count_over_time({container="n-bang-app"} | json [1h]))
```

---

## 11. 로그 관리 (MDC + Rolling + 구조화 로깅)

### 로그 관리 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   Log Flow                                              │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│   HTTP Request                                                                          │
│        │                                                                                │
│        ▼                                                                                │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│   │                           MdcLoggingFilter                                       │  │
│   │                                                                                  │  │
│   │   • traceId 생성/전파 (X-Trace-ID 헤더)                                          │  │
│   │   • requestId 생성 (X-Request-ID 헤더)                                           │  │
│   │   • clientIp 추출 (X-Forwarded-For)                                              │  │
│   │   • 요청/응답 로깅 (URI, Method, Status, Duration)                               │  │
│   │                                                                                  │  │
│   └───────────────────────────────────┬─────────────────────────────────────────────┘  │
│                                       │                                                 │
│                                       ▼                                                 │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│   │                             MDC Context                                          │  │
│   │                                                                                  │  │
│   │   ThreadLocal Map:                                                               │  │
│   │   ┌────────────────────────────────────────────────────────────┐                │  │
│   │   │  traceId      = "a1b2c3d4e5f6g7h8"                         │                │  │
│   │   │  requestId    = "h8g7f6e5d4c3b2a1"                         │                │  │
│   │   │  clientIp     = "123.45.67.89"                             │                │  │
│   │   │  requestUri   = "/api/v1/gatherings"                       │                │  │
│   │   │  requestMethod = "POST"                                    │                │  │
│   │   └────────────────────────────────────────────────────────────┘                │  │
│   │                                                                                  │  │
│   └───────────────────────────────────┬─────────────────────────────────────────────┘  │
│                                       │                                                 │
│                                       ▼                                                 │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│   │                         Logback (logback-spring.xml)                             │  │
│   │                                                                                  │  │
│   │   ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                 │  │
│   │   │ Console Appender │  │ File Appender   │  │ Async Appender  │                 │  │
│   │   │  (개발용)        │  │  (JSON 형식)    │  │  (성능 최적화)  │                 │  │
│   │   └────────┬────────┘  └────────┬────────┘  └────────┬────────┘                 │  │
│   │            │                    │                    │                           │  │
│   │            ▼                    ▼                    ▼                           │  │
│   │   ┌─────────────┐   ┌──────────────────────────────────────────┐                │  │
│   │   │   stdout    │   │              Log Files                   │                │  │
│   │   └─────────────┘   │  • application.log (JSON)                │                │  │
│   │                     │  • error.log (에러만)                    │                │  │
│   │                     │  • access.log (요청 로그)                │                │  │
│   │                     └──────────────────────────────────────────┘                │  │
│   │                                        │                                         │  │
│   └────────────────────────────────────────┼────────────────────────────────────────┘  │
│                                            │                                            │
│                                            ▼                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│   │                           Rolling Policy                                         │  │
│   │                                                                                  │  │
│   │   • 파일당 최대 크기: 100MB                                                      │  │
│   │   • 보관 기간: 15일                                                              │  │
│   │   • 전체 최대 크기: 3GB                                                          │  │
│   │   • 압축: gzip (.log.gz)                                                         │  │
│   │                                                                                  │  │
│   └─────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 파일 구조

```
src/main/
├── kotlin/com/nbang/nbangapi/support/logging/
│   ├── MdcLoggingFilter.kt      # MDC 필터 (요청 추적)
│   ├── MdcTaskDecorator.kt      # 비동기 작업 MDC 전파
│   └── LoggingConfig.kt         # 로깅 설정
│
└── resources/
    └── logback-spring.xml       # Logback 설정

logs/                            # 로그 디렉토리 (런타임)
├── application.log              # 메인 로그 (JSON)
├── application-text.log         # 메인 로그 (Plain Text)
├── error.log                    # 에러 로그
├── access.log                   # 접근 로그
└── archive/                     # 롤링된 로그
    ├── application.2024-01-15.0.log.gz
    ├── application.2024-01-15.1.log.gz
    └── ...
```

---

### MDC Filter

#### MdcLoggingFilter.kt

```kotlin
package com.nbang.nbangapi.support.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TRACE_ID = "traceId"
        const val REQUEST_ID = "requestId"
        const val CLIENT_IP = "clientIp"
        const val REQUEST_URI = "requestUri"
        const val REQUEST_METHOD = "requestMethod"

        private const val HEADER_X_REQUEST_ID = "X-Request-ID"
        private const val HEADER_X_TRACE_ID = "X-Trace-ID"
        private const val HEADER_X_FORWARDED_FOR = "X-Forwarded-For"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()

        try {
            setupMdc(request)
            addResponseHeaders(response)
            logRequest(request)

            filterChain.doFilter(request, response)

            logResponse(request, response, startTime)
        } finally {
            clearMdc()
        }
    }

    private fun setupMdc(request: HttpServletRequest) {
        val traceId = request.getHeader(HEADER_X_TRACE_ID) ?: generateId()
        val requestId = request.getHeader(HEADER_X_REQUEST_ID) ?: generateId()
        val clientIp = extractClientIp(request)

        MDC.put(TRACE_ID, traceId)
        MDC.put(REQUEST_ID, requestId)
        MDC.put(CLIENT_IP, clientIp)
        MDC.put(REQUEST_URI, request.requestURI)
        MDC.put(REQUEST_METHOD, request.method)
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR)
        return if (!xForwardedFor.isNullOrBlank()) {
            xForwardedFor.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }

    private fun generateId(): String =
        UUID.randomUUID().toString().replace("-", "").take(16)

    private fun clearMdc() {
        MDC.remove(TRACE_ID)
        MDC.remove(REQUEST_ID)
        MDC.remove(CLIENT_IP)
        MDC.remove(REQUEST_URI)
        MDC.remove(REQUEST_METHOD)
    }
}
```

#### MDC 컨텍스트 값

| Key | 설명 | 예시 |
|-----|------|------|
| `traceId` | 분산 추적 ID (서비스 간 전파) | `a1b2c3d4e5f6g7h8` |
| `requestId` | 개별 요청 ID | `h8g7f6e5d4c3b2a1` |
| `clientIp` | 클라이언트 IP | `123.45.67.89` |
| `requestUri` | 요청 URI | `/api/v1/gatherings` |
| `requestMethod` | HTTP 메서드 | `POST` |

---

### Logback 설정

#### logback-spring.xml 주요 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">

    <!-- 설정 값 -->
    <property name="LOG_DIR" value="${LOG_PATH:-./logs}" />
    <property name="MAX_HISTORY" value="15" />          <!-- 보관 기간 (일) -->
    <property name="MAX_FILE_SIZE" value="100MB" />     <!-- 파일당 최대 크기 -->
    <property name="TOTAL_SIZE_CAP" value="3GB" />      <!-- 전체 최대 크기 -->

    <!-- JSON 로그 패턴 (Loki 연동용) -->
    <property name="FILE_PATTERN_JSON"
              value='{"@timestamp":"%d{yyyy-MM-dd'\''T'\''HH:mm:ss.SSSZ}",
                      "level":"%level",
                      "logger_name":"%logger",
                      "message":"%msg",
                      "traceId":"%X{traceId:-}",
                      "requestId":"%X{requestId:-}",
                      "clientIp":"%X{clientIp:-}",
                      "thread":"%thread",
                      "app":"n-bang-api"}%n' />

    <!-- Rolling File Appender -->
    <appender name="APP_LOG_JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_DIR}/application.log</file>
        <encoder>
            <pattern>${FILE_PATTERN_JSON}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- 파일명: application.2024-01-15.0.log.gz -->
            <fileNamePattern>${LOG_DIR}/archive/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>${MAX_FILE_SIZE}</maxFileSize>
            <maxHistory>${MAX_HISTORY}</maxHistory>
            <totalSizeCap>${TOTAL_SIZE_CAP}</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <!-- Async Appender (성능 향상) -->
    <appender name="ASYNC_APP_LOG" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="APP_LOG_JSON" />
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <neverBlock>true</neverBlock>
    </appender>

    <!-- Production Profile -->
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="CONSOLE" />
            <appender-ref ref="ASYNC_APP_LOG" />
            <appender-ref ref="ASYNC_ERROR_LOG" />
        </root>
    </springProfile>

</configuration>
```

---

### 로그 파일 설명

| 파일 | 내용 | 형식 | 보관 |
|------|------|------|------|
| `application.log` | 전체 애플리케이션 로그 | JSON | 15일 |
| `application-text.log` | 전체 로그 (가독성용) | Plain Text | 15일 |
| `error.log` | ERROR 레벨 이상만 | JSON | 15일 |
| `access.log` | HTTP 요청/응답 로그 | JSON | 15일 |

---

### 로그 출력 예시

#### JSON 형식 (application.log)

```json
{
  "@timestamp": "2024-01-15T14:30:45.123+0900",
  "level": "INFO",
  "logger_name": "c.n.n.s.logging.MdcLoggingFilter",
  "message": ">>> REQUEST: POST /api/v1/gatherings | Client: 123.45.67.89",
  "traceId": "a1b2c3d4e5f6g7h8",
  "requestId": "h8g7f6e5d4c3b2a1",
  "clientIp": "123.45.67.89",
  "requestUri": "/api/v1/gatherings",
  "requestMethod": "POST",
  "thread": "http-nio-8080-exec-1",
  "app": "n-bang-api"
}
```

#### Console 형식 (개발용)

```
2024-01-15 14:30:45.123  INFO [http-nio-8080-exec-1] [a1b2c3d4e5f6g7h8] c.n.n.s.l.MdcLoggingFilter - >>> REQUEST: POST /api/v1/gatherings | Client: 123.45.67.89
2024-01-15 14:30:45.234  INFO [http-nio-8080-exec-1] [a1b2c3d4e5f6g7h8] c.n.n.a.g.GatheringFacade - Creating gathering: name=회식
2024-01-15 14:30:45.345  INFO [http-nio-8080-exec-1] [a1b2c3d4e5f6g7h8] c.n.n.s.l.MdcLoggingFilter - <<< RESPONSE: POST /api/v1/gatherings | Status: 201 | Duration: 222ms
```

---

### Rolling Policy 설정

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Rolling Policy 동작                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   application.log (현재 파일)                                                │
│        │                                                                    │
│        │  100MB 도달 또는 날짜 변경 시                                       │
│        ▼                                                                    │
│   archive/                                                                  │
│   ├── application.2024-01-15.0.log.gz  (첫 번째 롤링)                       │
│   ├── application.2024-01-15.1.log.gz  (두 번째 롤링, 같은 날)              │
│   ├── application.2024-01-14.0.log.gz                                       │
│   ├── application.2024-01-13.0.log.gz                                       │
│   └── ...                                                                   │
│                                                                             │
│   정리 규칙:                                                                 │
│   ├── 15일이 지난 파일 → 자동 삭제                                          │
│   ├── 전체 크기 3GB 초과 시 → 오래된 파일부터 삭제                           │
│   └── 시작 시 오래된 파일 정리 (cleanHistoryOnStart=true)                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 비동기 작업에서 MDC 전파

`@Async` 어노테이션 사용 시 MDC 컨텍스트가 유실되지 않도록 `MdcTaskDecorator`를 사용합니다.

#### MdcTaskDecorator.kt

```kotlin
class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            try {
                contextMap?.let { MDC.setContextMap(it) }
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}
```

#### LoggingConfig.kt

```kotlin
@Configuration
class LoggingConfig {
    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.setTaskDecorator(MdcTaskDecorator())
        executor.initialize()
        return executor
    }
}
```

---

### Docker 환경 설정

#### docker-compose.prod.yml 수정

```yaml
services:
  app:
    image: n-bang-api:latest
    container_name: n-bang-app
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - LOG_PATH=/app/logs
    volumes:
      - app_logs:/app/logs    # 로그 볼륨 마운트
    logging:
      driver: "json-file"
      options:
        max-size: "50m"       # Docker 로그 크기 제한
        max-file: "5"         # Docker 로그 파일 수 제한

volumes:
  app_logs:
```

---

### Loki와 연동

JSON 형식 로그는 Alloy가 자동으로 파싱하여 Loki로 전송합니다.

#### Grafana LogQL 쿼리 예시

```logql
# traceId로 전체 요청 추적
{app="n-bang-api"} | json | traceId="a1b2c3d4e5f6g7h8"

# 특정 사용자 IP의 요청 조회
{app="n-bang-api"} | json | clientIp="123.45.67.89"

# 응답 시간이 긴 요청 조회 (Duration 포함된 로그)
{app="n-bang-api"} | json | message=~".*Duration.*" | pattern "<_> Duration: <duration>ms" | duration > 1000

# 에러 발생한 traceId 조회
{app="n-bang-api"} | json | level="ERROR" | line_format "{{.traceId}}"

# 특정 API 호출 통계
count_over_time({app="n-bang-api"} | json | requestUri="/api/v1/gatherings" [1h])
```

---

### 로그 레벨 가이드

| 레벨 | 용도 | 예시 |
|------|------|------|
| **ERROR** | 즉시 대응 필요한 오류 | DB 연결 실패, 외부 API 오류 |
| **WARN** | 잠재적 문제 | 재시도 성공, 폐기 예정 API 사용 |
| **INFO** | 주요 비즈니스 이벤트 | 요청/응답, 주요 처리 완료 |
| **DEBUG** | 개발/디버깅용 상세 정보 | 파라미터 값, 중간 처리 결과 |
| **TRACE** | 매우 상세한 추적 정보 | SQL 바인딩 값 |

---

### 로그 모니터링 대시보드

Grafana에서 다음 패널을 구성할 수 있습니다:

| 패널 | 쿼리 | 용도 |
|------|------|------|
| 에러율 | `sum(rate({app="n-bang-api"} \| json \| level="ERROR"[5m]))` | 에러 발생 추이 |
| 요청 수 | `count_over_time({app="n-bang-api"} \| json \| message=~"REQUEST"[1m])` | 트래픽 모니터링 |
| 응답 시간 분포 | Duration 추출 후 히스토그램 | 성능 모니터링 |
| 최근 에러 로그 | `{app="n-bang-api"} \| json \| level="ERROR"` | 실시간 에러 확인 |
