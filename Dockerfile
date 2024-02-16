FROM eclipse-temurin:17-jdk-jammy AS build-stage

ADD . /app/
WORKDIR /app/

RUN chmod +x ./gradlew

RUN ./gradlew clean
RUN ./gradlew bootJar

FROM amd64/openjdk:18.0-oracle AS run-stage

ADD . /app/
WORKDIR /app/
COPY --from=build-stage /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java","-jar","app.jar"]