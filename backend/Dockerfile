FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY build/libs/*.jar map.jar
ENTRYPOINT ["java","-jar","/map.jar"]