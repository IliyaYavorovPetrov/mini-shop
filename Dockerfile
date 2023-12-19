FROM amazoncorretto:17-al2023-jdk AS build-stage

ADD . /app/
WORKDIR /app/

RUN ./gradlew clean
RUN ./gradlew bootJar --args='--spring.profiles.active=prod'

FROM amazoncorretto:17-al2023-jdk AS run-stage

ADD . /app/
WORKDIR /app/
COPY --from=build-stage /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","app.jar"]