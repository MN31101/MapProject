FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY build/libs/ /app/
ENTRYPOINT ["sh", "-c", "java -jar /app/map-0.0.1-SNAPSHOT.jar"]
