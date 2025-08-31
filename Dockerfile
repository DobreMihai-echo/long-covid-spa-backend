# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Leverage Docker layer caching
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy
# Run as non-root
RUN useradd -ms /bin/bash appuser
WORKDIR /app

# Copy the fat jar
COPY --from=build /app/target/*.jar app.jar

# Container-friendly JVM defaults
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
# Default profile = production (can be overridden by env SPRING_PROFILES_ACTIVE)
ENV SPRING_PROFILES_ACTIVE=production

EXPOSE 8080
USER appuser
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
