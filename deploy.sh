docker run -d \
    --name api \
    --rm \
    -p 80:8081 \
    --env-file .env \
    xdainz/api-pedidos:latest
