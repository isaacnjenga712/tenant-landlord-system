
# Deployment



## Prerequisites



- Ubuntu 22.04+ (or Debian 12+)

- Docker Engine 24+ and Docker Compose plugin 2.20+

- A domain with A records for `api.yourdomain.com` and `app.yourdomain.com`

- Ports 80 and 443 reachable from the internet



## 1. Firewall



**Critical:** the `docker-compose.prod.yml` exposes internal service ports

for convenience. The firewall must block them.



    sudo ufw default deny incoming

    sudo ufw default allow outgoing

    sudo ufw allow 22/tcp

    sudo ufw allow 80/tcp

    sudo ufw allow 443/tcp

    sudo ufw enable



Verify only 22, 80, 443 are reachable from outside.



Cloud VMs (EC2, DigitalOcean, Hetzner) should also restrict the

security group to the same three ports.



## 2. Clone + configure



    git clone <repo> && cd tenant-landlord-system

    cp .env.prod.example .env.prod

    # Edit .env.prod with real values:

    #   POSTGRES_PASSWORD, MONGO_PASSWORD, REDIS_PASSWORD (openssl rand -base64 32)

    #   JWT_ACCESS_SECRET, JWT_REFRESH_SECRET (openssl rand -hex 64)

    #   CORS_ALLOWED_ORIGINS=https://app.yourdomain.com

    #   SPRING_MAIL_* , DARAJA_* , etc.



## 3. TLS certificates



    sudo apt-get install -y certbot

    sudo certbot certonly --standalone -d api.yourdomain.com -d app.yourdomain.com

    sudo cp /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem nginx/certs/

    sudo cp /etc/letsencrypt/live/api.yourdomain.com/privkey.pem nginx/certs/

    sudo chmod 644 nginx/certs/*.pem



Auto-renewal (add to crontab):



    0 3 * * * certbot renew --quiet && docker compose -f docker-compose.yml -f docker-compose.prod.yml restart nginx



## 4. Frontend build



    cd frontend

    npm ci

    VITE_API_URL=https://api.yourdomain.com npm run build

    cd ..



## 5. Deploy



    ./scripts/deploy.sh



Or manually:



    docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up -d --build



## 6. Verify



    # Gateway health

    curl -s https://api.yourdomain.com/actuator/health



    # Internal endpoints must return 404

    curl -s -o /dev/null -w "%{http_code}/n" https://api.yourdomain.com/api/v1/internal/users/count

    # → 404



    # Firewall blocks direct access

    curl -s -o /dev/null -w "%{http_code}/n" http://your-server-ip:8081/actuator/health

    # → connection refused or timeout



## 7. Backups



Postgres:



    0 2 * * * docker compose --env-file .env.prod exec -T postgres pg_dump -U rentflow rentflow | gzip > /backup/pg-$(date +/%F).sql.gz



MongoDB:



    0 3 * * * docker compose --env-file .env.prod exec -T mongodb mongodump --archive --gzip --username $MONGO_USER --password $MONGO_PASSWORD --authenticationDatabase admin > /backup/mongo-$(date +/%F).archive.gz



Test a restore monthly. A backup you've never restored is not a backup.

