FROM gradle:7.6-jdk-alpine AS build
WORKDIR /workspace/app

COPY . .
RUN gradle build -x test

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /workspace/app/build/libs/map-0.0.1-SNAPSHOT.jar map-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "map-0.0.1-SNAPSHOT.jar"]
