#!/bin/bash

APP_NAME="n-bang-api"
APP_HOME="/home/ec2-user/app"
PID_FILE="$APP_HOME/app.pid"

# PID 파일 확인
if [ ! -f "$PID_FILE" ]; then
    echo "$APP_NAME is not running (PID file not found)"
    exit 0
fi

PID=$(cat "$PID_FILE")

# 프로세스 확인 및 종료
if ps -p $PID > /dev/null 2>&1; then
    echo "Stopping $APP_NAME (PID: $PID)..."
    kill $PID

    # Graceful shutdown 대기 (최대 30초)
    for i in {1..30}; do
        if ! ps -p $PID > /dev/null 2>&1; then
            echo "$APP_NAME stopped successfully"
            rm -f "$PID_FILE"
            exit 0
        fi
        sleep 1
    done

    # 강제 종료
    echo "Graceful shutdown timeout, forcing stop..."
    kill -9 $PID
    rm -f "$PID_FILE"
    echo "$APP_NAME force stopped"
else
    echo "$APP_NAME is not running (PID $PID not found)"
    rm -f "$PID_FILE"
fi

exit 0
