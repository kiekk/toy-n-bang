#!/bin/bash

APP_NAME="n-bang-api"
APP_HOME="/home/ec2-user/app"
JAR_FILE="$APP_HOME/app.jar"
PID_FILE="$APP_HOME/app.pid"
LOG_FILE="$APP_HOME/app.log"

# 환경변수 로드
if [ -f /etc/profile.d/nbang-env.sh ]; then
    source /etc/profile.d/nbang-env.sh
fi

# JAR 파일 존재 확인
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi

# 환경변수 확인
if [ -z "$JASYPT_PASSWORD" ]; then
    echo "Error: JASYPT_PASSWORD not set"
    exit 1
fi

# 애플리케이션 시작
echo "Starting $APP_NAME..."
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Djasypt.encryptor.password="${JASYPT_PASSWORD}" \
    "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &

# PID 저장
echo $! > "$PID_FILE"

echo "$APP_NAME started with PID $(cat $PID_FILE)"
echo "Logs: $LOG_FILE"

# 시작 대기 및 확인
sleep 5
if ps -p $(cat $PID_FILE) > /dev/null 2>&1; then
    echo "$APP_NAME is running"
    exit 0
else
    echo "Error: $APP_NAME failed to start"
    tail -20 "$LOG_FILE"
    exit 1
fi
