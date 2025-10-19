FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
RUN chmod +x gradlew
COPY gradle /app/gradle

COPY build.gradle.kts ../settings.gradle.kts ./

RUN ./gradlew dependencies

COPY src/main /app/src/main

RUN ./gradlew bootJar --no-daemon

# -----

FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar caramel-batch.jar
USER caramel-batch
ENTRYPOINT ["java", "-jar", "caramel-batch.jar"]
