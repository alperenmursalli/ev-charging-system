#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

APP_LOCAL_PORT="${APP_LOCAL_PORT:-8083}"
JDBC_URL="${SPRING_DATASOURCE_URL:?SPRING_DATASOURCE_URL is required}"
if [[ "$JDBC_URL" != *"prepareThreshold="* ]]; then
  if [[ "$JDBC_URL" == *"?"* ]]; then
    JDBC_URL="${JDBC_URL}&prepareThreshold=0"
  else
    JDBC_URL="${JDBC_URL}?prepareThreshold=0"
  fi
fi

./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.arguments="--server.port=${APP_LOCAL_PORT} --spring.datasource.url=${JDBC_URL} --spring.datasource.username=${SPRING_DATASOURCE_USERNAME:?SPRING_DATASOURCE_USERNAME is required} --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:?SPRING_DATASOURCE_PASSWORD is required} --spring.jpa.hibernate.ddl-auto=update --spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect --spring.datasource.driver-class-name=org.postgresql.Driver"
