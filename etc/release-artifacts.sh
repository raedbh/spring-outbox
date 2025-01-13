#!/bin/bash

MODULES=$(mvn help:evaluate -Dexpression=project.modules -q -DforceStdout | \
sed -n '/<string>/ s/.*<string>\(.*\)<\/string>.*/\1/p' | \
grep -v -E '^(spring-outbox-debezium-connector.*|spring-outbox-sample|spring-outbox-integration-tests)$' | \
tr '\n' ',' | \
sed 's/,$//')

if [ -z "$MODULES" ]; then
  echo "No modules found to build!"
  exit 1
fi

./mvnw -Prelease deploy -s etc/settings.xml  -pl "$MODULES" -am -DskipTests

exit $?
