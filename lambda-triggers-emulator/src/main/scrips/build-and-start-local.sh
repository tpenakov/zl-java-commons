#!/usr/bin/env bash


IMAGE_NAME=zl-lambda-trigger-emulators-local
SERVICE_NAME=zl-lambda-trigger-emulators-local-dev

#https://docs.docker.com/engine/reference/commandline/run/#add-host
LOCAL_IP=${LOCAL_IP_GLOBAL:-10.1.1.205}

DIR="$(dirname $0)"
cd "${DIR}/../../.." || exit 1
pwd

docker stop ${SERVICE_NAME}
docker rmi $IMAGE_NAME
gradle clean
gradle build
docker build -t $IMAGE_NAME -f src/main/docker/Dockerfile.jvm .

docker run --rm -it -d -p 9000:8080 \
  --add-host="pc:${LOCAL_IP}" \
  --name=${SERVICE_NAME} \
  -e "W3_SQS_URL=http://pc:4566/000000000000/sqs-queue-zlmvp-w3-dev.fifo" \
  -e "W3_SQS_LAMBDA_URL=http://pc:9002" \
  -e "DB_SQS_URL=http://pc:4566/000000000000/sqs-queue-zlmvp-db-dev.fifo" \
  -e "DB_SQS_LAMBDA_URL=http://pc:9001" \
  -e "DB_WITH_DUPLICATION_SQS_URL=http://pc:4566/000000000000/sqs-queue-zlmvp-db-dev-with-duplication.fifo" \
  -e "DB_WITH_DUPLICATION_SQS_LAMBDA_URL=http://pc:9011" \
  -e "W3_RETRY_SQS_URL=http://pc:4566/000000000000/sqs-queue-zlmvp-w3-dev-retry.fifo" \
  -e "W3_RETRY_SQS_LAMBDA_URL=http://pc:9012" \
  -e "S3_SQS_URL=http://pc:4566/000000000000/sqs-queue-zlmvp-s3-dev.fifo" \
  -e "S3_SQS_LAMBDA_URL=http://pc:9003" \
  -e "S3_RETRY_SQS_URL=http://pc:4566/000000000000/sqs-queue-zlmvp-w3-dev-retry.fifo" \
  -e "S3_RETRY_SQS_LAMBDA_URL=http://pc:9013" \
  -e "QUARKUS_SQS_ENDPOINT_OVERRIDE=http://pc:4566" \
  -e "QUARKUS_SQS_AWS_REGION=us-east-1" \
  -e "QUARKUS_SQS_AWS_CREDENTIALS_TYPE=static" \
  -e "QUARKUS_SQS_AWS_CREDENTIALS_STATIC_PROVIDER_ACCESS_KEY_ID=test-key" \
  -e "QUARKUS_SQS_AWS_CREDENTIALS_STATIC_PROVIDER_SECRET_ACCESS_KEY=test-secret" \
  $IMAGE_NAME
