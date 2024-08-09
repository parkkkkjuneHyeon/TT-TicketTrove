FROM eclipse-temurin:21
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]