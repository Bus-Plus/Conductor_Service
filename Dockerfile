FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/conductor_service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java","-jar","app.jar"]