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

# Artifact id is "discovery-server"
COPY --from=builder /app/target/discovery-server-0.0.1-SNAPSHOT.jar app.jar

# Eureka Server listens on port 8761
EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]