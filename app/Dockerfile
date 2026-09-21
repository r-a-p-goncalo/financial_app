#we start by using an available image with maven, calling it build so we can later access its output
FROM maven:3.9-eclipse-temurin-21 AS build

#changes the working directory to the temporary build workspace
WORKDIR /workspace

#copies the Maven project configuration before the source code, so Docker can cache dependency downloads
COPY pom.xml ./

#runs Maven in non-interactive mode without transfer progress, downloading dependencies for the build cache
RUN mvn -B -ntp dependency:go-offline

#copies the application source code into the build workspace
COPY src ./src

#runs Maven through the package phase without tests, which produces the Spring Boot JAR
#tests run separately in CI, where PostgreSQL Testcontainers can use Docker
RUN mvn -B -ntp package -DskipTests

#uses a smaller Java runtime image for the production container
FROM eclipse-temurin:21-jre-jammy

#creates a non-root user that will run the application
RUN groupadd --system --gid 10001 financialapp \
    && useradd --system --uid 10001 --gid financialapp \
        --home-dir /opt/financial-app --create-home financialapp

#sets the directory from which the application will run
WORKDIR /opt/financial-app

#copies only the packaged JAR from the build stage and gives the application user ownership
COPY --from=build --chown=financialapp:financialapp \
    /workspace/target/financial-app-*.jar app.jar

#runs the container without root privileges
USER financialapp

#documents the port on which the Spring Boot application listens
EXPOSE 8080

#starts the packaged application when the container starts
ENTRYPOINT ["java", "-jar", "/opt/financial-app/app.jar"]
