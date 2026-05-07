# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Cache dependencies before copying full source
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Artifact id is "Backend", same pattern as the other services
COPY --from=builder /app/target/Backend-0.0.1-SNAPSHOT.jar app.jar

# BacklogService listens on port 8083
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]