#!/bin/bash

DOCKER_USERNAME="raed"
CONNECTORS_BASE_DIR="spring-outbox-debezium-connectors"
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# List of connectors to release
CONNECTORS=$(find "$CONNECTORS_BASE_DIR" -maxdepth 1 -type d -name "spring-outbox-debezium-connector-*" -exec basename {} \; | tr '\n' ',' | sed 's/,$//')

IFS=','
for CONNECTOR in $CONNECTORS; do
  echo -e "\033[1;34m=========================================================\033[0m"
  echo -e "\033[1;32m🚀 Building and pushing Docker image for: \033[1;33m$CONNECTOR\033[0m"
  echo -e "\033[1;34m=========================================================\033[0m"


  IMAGE_NAME="$DOCKER_USERNAME/$CONNECTOR:$VERSION"

  # Navigate to connector directory
  cd "$CONNECTORS_BASE_DIR" && cd "$CONNECTOR" || exit

  # Build Docker image
  docker build -t "$IMAGE_NAME" .

  if [ $? -ne 0 ]; then
    echo "Failed to build Docker image for $CONNECTOR. Skipping..."
    cd ..
    continue
  fi

  # Push Docker image
  docker push "$IMAGE_NAME"

  if [ $? -eq 0 ]; then
    echo "Successfully pushed $IMAGE_NAME."
  else
    echo "Failed to push $IMAGE_NAME."
  fi

  cd ../..

done
unset IFS

exit 0
