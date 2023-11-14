FROM amd64/openjdk:18.0-oracle AS build-stage

ADD . /app/
WORKDIR /app/

RUN ./gradlew clean
RUN ./gradlew bootJar --args='--spring.profiles.active=prod'

FROM amd64/openjdk:18.0-oracle AS run-stage

ADD . /app/
WORKDIR /app/
COPY --from=build-stage /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","app.jar"]