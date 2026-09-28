# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# We already know the app has run the tests correctly
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app
COPY --from=build ./build/target/api-*.jar ./api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api.jar"]