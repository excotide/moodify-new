# syntax=docker/dockerfile:1.6

# ---- Build stage ----------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy source
COPY pom.xml ./
COPY src ./src

# Build the application (tests can be re-enabled if desired)
RUN mvn -B -DskipTests clean package

# ---- Runtime stage --------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Create non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Copy fat jar from the build stage
COPY --from=build /workspace/target/moodify-springboot-1.0-SNAPSHOT.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS=""

EXPOSE 8080
USER spring

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
