docker run -d \
    --rm \
    --name db \
    -p 3306:3306 \
    -v mysql_data:/var/lib/mysql \
    -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
    -e MYSQL_DATABASE=db_pedidos \
    mysql:latest 