# ── Stage 1: Offline download all dependencies ──
FROM maven:3.8.5-openjdk-17 AS deps
WORKDIR /app

# Copy only pom.xml to cache this layer
COPY pom.xml ./
# Download all project dependencies to the local repository
RUN mvn dependency:go-offline -B

# ── Stage 2: Build and package ──
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Reuse local repository from deps stage to speed up dependency resolution
COPY --from=deps /root/.m2 /root/.m2

# Copy all source code and package (skip tests)
COPY . .
RUN mvn clean package -DskipTests -B

# ── Stage 3: Lightweight runtime ──
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the compiled JAR
COPY --from=build /app/target/*.jar app.jar

# Startup command
ENTRYPOINT ["java","-jar","app.jar"]
