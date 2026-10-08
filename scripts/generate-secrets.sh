
#!/usr/bin/env bash

set -euo pipefail



if ! command -v openssl >/dev/null 2>&1; then

    echo "Error: openssl not found. Install it and retry." >&2

    exit 1

fi



gen_hex() {

    openssl rand -hex "${1:-32}"

}



gen_b64() {

    openssl rand -base64 "${1:-24}" | tr -d '/+=' | cut -c1-"${2:-32}"

}



cat <<GEN_END

# Generated: $(date -u +"%Y-%m-%dT%H:%M:%SZ")



# ---------- Databases ----------

POSTGRES_DB=rentflow

POSTGRES_USER=rentflow

POSTGRES_PASSWORD=$(gen_b64 24 32)



MONGO_USER=rentflow

MONGO_PASSWORD=$(gen_b64 24 32)



REDIS_PASSWORD=$(gen_b64 24 32)



# ---------- JWT ----------

JWT_ACCESS_SECRET=$(gen_hex 64)

JWT_REFRESH_SECRET=$(gen_hex 64)



# ---------- CORS ----------

CORS_ALLOWED_ORIGINS=http://localhost:5173



# ---------- Email ----------

SPRING_MAIL_HOST=smtp.gmail.com

SPRING_MAIL_PORT=587

SPRING_MAIL_USERNAME=

SPRING_MAIL_PASSWORD=



# ---------- M-Pesa ----------

DARAJA_CONSUMER_KEY=

DARAJA_CONSUMER_SECRET=

DARAJA_PASSKEY=

DARAJA_SHORTCODE=174379

DARAJA_CALLBACK_URL=http://localhost:8089/api/v1/mpesa/callback



# ---------- SMS / WhatsApp ----------

SMS_GATEWAY_URL=

WHATSAPP_GATEWAY_URL=

WHATSAPP_FROM=



# ---------- Observability ----------

GRAFANA_ADMIN_USER=admin

GRAFANA_ADMIN_PASSWORD=$(gen_b64 24 32)

TRACING_SAMPLE_RATE=1.0



# ---------- Runtime ----------

SPRING_PROFILES_ACTIVE=docker

LOG_LEVEL_NOTIFICATION=DEBUG

LOG_LEVEL_ADMIN=DEBUG

GEN_END

