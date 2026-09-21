FROM maven:3.9-eclipse-temurin-21 AS dependencies

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

FROM dependencies AS test

COPY src ./src
RUN mvn -B -ntp verify

FROM dependencies AS package

COPY src ./src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system --gid 10001 financialapp \
    && useradd --system --uid 10001 --gid financialapp \
        --home-dir /opt/financial-app --create-home financialapp \
    && mkdir -p /var/lib/financial-app \
    && chown -R financialapp:financialapp \
        /opt/financial-app /var/lib/financial-app

WORKDIR /opt/financial-app

COPY --from=package --chown=financialapp:financialapp \
    /workspace/target/financial-app-*.jar app.jar

USER financialapp

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/financial-app/app.jar"]
