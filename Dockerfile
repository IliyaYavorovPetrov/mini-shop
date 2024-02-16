FROM eclipse-temurin:17-jdk-jammy AS base
WORKDIR /app
COPY . .

FROM base AS build
RUN ./gradlew build -x test

FROM build AS run
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]