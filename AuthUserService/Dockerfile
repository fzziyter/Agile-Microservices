# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy dependency descriptors first for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the fat JAR produced by the build stage
COPY --from=builder /app/target/Backend-0.0.1-SNAPSHOT.jar app.jar

# AuthUserService listens on port 8081
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]