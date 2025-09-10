## ---------- Build stage ----------
FROM gradle:8.14.3-jdk17-alpine AS build

WORKDIR /workspace

COPY . .

# Build only the app-service bootJar to speed up the build
#RUN ./gradlew :applications:app-service:bootJar --no-daemon -x test
RUN ./gradlew :app-service:bootJar --no-daemon -x test

## ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# The bootJar name is configured as the root project name with .jar extension (CrediYaVE.jar)

COPY --from=build /workspace/applications/app-service/build/libs/*.jar AUTENTICACION.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar AUTENTICACION.jar"]
