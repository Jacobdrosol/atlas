FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system --gid 10001 atlas \
    && useradd --system --uid 10001 --gid atlas --no-create-home --shell /usr/sbin/nologin atlas

WORKDIR /app
COPY --from=build /workspace/target/atlas-api-0.1.0.jar /app/app.jar

USER 10001:10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
