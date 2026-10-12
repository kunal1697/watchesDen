#!/usr/bin/env bash
# Runs the whole WatchesDen backend without Docker or PostgreSQL (Java 11+ only).
# Data is kept in embedded H2 databases under ~/.watchesden (override with AEON_DATA_DIR).
# Usage: ./run-local.sh            build (if needed) and start everything; Ctrl+C stops it all
#        ./run-local.sh --build    force a rebuild first
set -euo pipefail

cd "$(dirname "$0")/backend"
mkdir -p ../logs

if [[ "${1:-}" == "--build" || ! -f api-gateway/target/api-gateway-1.0.0.jar ]]; then
  echo "Building backend (first run downloads dependencies, this can take a few minutes)..."
  ./mvnw -B -q package -DskipTests
fi

pids=()
cleanup() {
  echo; echo "Stopping services..."
  kill "${pids[@]}" 2>/dev/null || true
  wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

start() {
  local name=$1; shift
  java -jar "$name/target/$name-1.0.0.jar" "$@" > "../logs/$name.log" 2>&1 &
  pids+=($!)
  echo "  started $name (log: logs/$name.log)"
}

wait_for() {
  local url=$1 name=$2
  for _ in $(seq 1 60); do
    curl -fs "$url" > /dev/null 2>&1 && return 0
    sleep 2
  done
  echo "$name did not come up, see logs/$name.log"; exit 1
}

echo "Starting discovery-server..."
start discovery-server
wait_for http://localhost:8761/actuator/health discovery-server

echo "Starting services..."
start user-service --spring.profiles.active=local
start product-service --spring.profiles.active=local
start order-service --spring.profiles.active=local
start api-gateway

wait_for http://localhost:8080/actuator/health api-gateway
wait_for http://localhost:8081/actuator/health user-service
wait_for http://localhost:8082/actuator/health product-service
wait_for http://localhost:8083/actuator/health order-service

cat <<EOF

WatchesDen backend is up (allow ~30s more for the gateway to discover every service).
  API gateway : http://localhost:8080
  Swagger UI  : http://localhost:8080/swagger-ui.html
  Eureka      : http://localhost:8761
Start the frontend in another terminal:  cd frontend && npm install && npm run dev
Press Ctrl+C to stop.
EOF
wait
