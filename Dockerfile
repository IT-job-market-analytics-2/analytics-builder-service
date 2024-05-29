#
# Build stage
#
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /analytics-builder-service
COPY pom.xml .
RUN mvn verify
COPY . .
RUN ["mvn", "package", "-Dmaven.test.skip=true"]

#
# Package stage
#
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /analytics-builder-service
COPY --from=build /analytics-builder-service/target/*.jar analytics-builder-service.jar
ENTRYPOINT ["java", "-jar", "analytics-builder-service.jar" ]