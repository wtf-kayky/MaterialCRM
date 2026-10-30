FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S materialcrm && adduser -S materialcrm -G materialcrm
COPY --from=build /workspace/target/materialcrm-api-0.1.0-SNAPSHOT.jar app.jar
USER materialcrm
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

