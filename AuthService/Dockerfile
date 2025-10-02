# Use OpenJDK 17 slim base image
FROM openjdk:17-jdk-slim

# Working directory
WORKDIR /app

# Copy Maven built jar (adjust name to your artifact)
COPY target/authservice.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
