
#!/bin/bash

echo "=== RentFlow Health Check ==="

for port in 8761 8080 8081 8082 8083 8084 8085 8086 8087; do

  if [ $port -eq 8761 ]; then

    url="http://localhost:$port/"

  else

    url="http://localhost:$port/actuator/health"

  fi

  status=$(curl -s -o /dev/null -w "%{http_code}" $url)

  if [ $status -eq 200 ]; then echo "OK $port - UP ($url)"; else echo "FAIL $port - DOWN ($status) $url"; fi

done

