#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

APP_LOCAL_PORT="${APP_LOCAL_PORT:-18083}"

./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.useTestClasspath=true \
  -Dspring-boot.run.arguments="--server.port=${APP_LOCAL_PORT} --spring.datasource.url=jdbc:h2:mem:evsystem;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.hibernate.ddl-auto=create-drop --spring.jpa.database-platform=org.hibernate.dialect.H2Dialect --spring.jpa.show-sql=false --schema.init.charging-session-status.enabled=false"
