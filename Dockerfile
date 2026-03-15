# Etape 1 : Build
FROM maven:3.9.3-eclipse-temurin-17 AS builder

WORKDIR /app

# Copier pom.xml
COPY pom.xml .


COPY src ./src

RUN mvn clean package -DskipTests

# Etape 2 : Runtime
FROM eclipse-temurin:17-jre-focal AS runtime

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]