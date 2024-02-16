FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends dos2unix

COPY . .

RUN dos2unix gradlew

RUN ./gradlew build -x test

EXPOSE 8080

COPY /build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]