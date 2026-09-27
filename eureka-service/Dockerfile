## ================================
## Build Stage
## ================================
#FROM maven:3.9.11-eclipse-temurin-25 AS builder
#
#WORKDIR /build
#
#COPY pom.xml .
#COPY mvnw .
#COPY .mvn .mvn
#
#RUN chmod +x mvnw
#
#RUN ./mvnw dependency:go-offline -B
#
#COPY src src
#
#RUN ./mvnw clean package -DskipTests
#
#
## ================================
## Runtime Stage
## ================================
#FROM eclipse-temurin:25-jre
#
#WORKDIR /app
#
#COPY --from=builder /build/target/*.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]