# Stage 1: Build the app with Maven
   FROM maven:3.9.6-eclipse-temurin-17 AS build
   WORKDIR /app
   COPY pom.xml .
   COPY src ./src
   RUN mvn clean package -DskipTests

   # Stage 2: Run it with just a lightweight JRE
   FROM eclipse-temurin:17-jre-alpine
   WORKDIR /app
   COPY --from=build /app/target/ecommerce-app-1.0.0.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]