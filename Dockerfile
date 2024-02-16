FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

EXPOSE 8080

COPY /build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]