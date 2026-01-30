#!/bin/bash

AWS_REGION="ap-northeast-2"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE_NAME="n-bang-api"
CONTAINER_NAME="n-bang-api"

# ECR 로그인
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

# 최신 이미지 pull
docker pull $ECR_REGISTRY/$IMAGE_NAME:latest

# 컨테이너 실행
docker run -d \
    --name $CONTAINER_NAME \
    --restart unless-stopped \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e JASYPT_ENCRYPTOR_PASSWORD="${JASYPT_PASSWORD}" \
    $ECR_REGISTRY/$IMAGE_NAME:latest

echo "Container $CONTAINER_NAME started successfully"
