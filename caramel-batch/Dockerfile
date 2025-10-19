FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
RUN chmod +x gradlew
COPY gradle /app/gradle

COPY settings.gradle.kts build.gradle.kts ./
COPY caramel-infrastructure caramel-infrastructure
COPY caramel-common caramel-common
COPY caramel-domain caramel-domain
COPY caramel-batch caramel-batch

RUN ./gradlew :caramel-batch:bootJar --no-daemon

# -----
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

COPY --from=builder /app/caramel-batch/build/libs/caramel-batch.jar caramel-batch.jar
USER caramel-batch
ENTRYPOINT ["java", "-jar", "caramel-batch.jar"]
