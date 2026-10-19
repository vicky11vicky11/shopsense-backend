#!/usr/bin/env bash

set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

SERVICES=(
    "catalog-service"
    "eureka-service"
    "gateway-service"
    "inventory-service"
    "media-service"
    "notification-service"
    "order-service"
    "payment-service"
    "user-service"
    "orchestrator-service"
)

for SERVICE in "${SERVICES[@]}"; do
    echo "========================================"
    echo "Building $SERVICE"
    echo "========================================"

    cd "$ROOT_DIR/$SERVICE"
    ./mvnw clean install -DskipTests
    cd "$ROOT_DIR"

    echo "✓ $SERVICE built successfully"
    echo
done

echo "========================================"
echo "All services built successfully!"
echo "========================================"