
#!/usr/bin/env bash

# ============================================================================

# Deploy RentFlow to production.

#

# Usage:

#   ./scripts/deploy.sh              # deploy with .env.prod

#   ./scripts/deploy.sh --build      # force rebuild images

#   ./scripts/deploy.sh --down       # stop everything

#   ./scripts/deploy.sh --health     # check status only

# ============================================================================



set -euo pipefail



ENV_FILE="${ENV_FILE:-.env.prod}"

COMPOSE="docker compose --env-file $ENV_FILE -f docker-compose.yml -f docker-compose.prod.yml"



if [ ! -f "$ENV_FILE" ]; then

    echo "Error: $ENV_FILE not found." >&2

    echo "Copy .env.prod.example to $ENV_FILE and fill in real values." >&2

    exit 1

fi



case "${1:-up}" in

    up|--up)

        echo "==> Building and starting stack..."

        $COMPOSE up -d --build

        echo ""

        echo "==> Waiting 90s for services to start..."

        sleep 90

        echo ""

        echo "==> Status:"

        $COMPOSE ps

        echo ""

        echo "==> Health check:"

        curl -sf http://localhost:8080/actuator/health | head -c 200 || echo "  gateway not responding yet"

        echo ""

        ;;

    --build)

        echo "==> Rebuilding all images..."

        $COMPOSE build --no-cache

        $COMPOSE up -d

        ;;

    --down)

        echo "==> Stopping stack..."

        $COMPOSE down

        ;;

    --health)

        $COMPOSE ps

        echo ""

        curl -sf http://localhost:8080/actuator/health || echo "  gateway not responding"

        ;;

    --logs)

        $COMPOSE logs -f --tail=100

        ;;

    *)

        echo "Usage: $0 [up|--build|--down|--health|--logs]" >&2

        exit 1

        ;;

esac

