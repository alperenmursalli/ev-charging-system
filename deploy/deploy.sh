#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/ev-charging-system}"
COMPOSE_BIN="${COMPOSE_BIN:-docker-compose}"

cd "$APP_DIR"

if [ ! -f ".env" ]; then
  echo ".env file is missing in $APP_DIR"
  echo "Create it from .env.example and fill production secrets first."
  exit 1
fi

git pull --ff-only
$COMPOSE_BIN up -d --build
$COMPOSE_BIN ps
