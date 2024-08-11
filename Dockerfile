FROM amazoncorretto:21
COPY gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

FROM amazoncorretto:21
COPY server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
