FROM bellsoft/liberica-openjre-debian:17-cds AS builder
WORKDIR /builder

COPY target/spring-outbox-debezium-connector-postgres-rabbit-*.jar application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjre-debian:17-cds

RUN addgroup --gid 999 appuser && \
adduser --uid 999 --gid 999 --home /home/appuser --disabled-password --system appuser

USER appuser:appuser
WORKDIR /application

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

ENTRYPOINT ["java", "-jar", "application.jar"]
