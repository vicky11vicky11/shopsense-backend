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
)

for SERVICE in "${SERVICES[@]}"; do
    echo "========================================"
    echo "Cleaning $SERVICE"
    echo "========================================"

    cd "$ROOT_DIR/$SERVICE"
    ./mvnw clean
    cd "$ROOT_DIR"

    echo "✓ $SERVICE cleaned successfully"
    echo
done

echo "========================================"
echo "All services cleaned successfully!"
echo "========================================"