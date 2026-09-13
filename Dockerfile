FROM gradle:9-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :infra:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/infra/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
