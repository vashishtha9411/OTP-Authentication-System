# Use official OpenJDK 17 image as base
FROM openjdk:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the jar file from host into the container
COPY target/auth-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 for the container
EXPOSE 8080

# Command to run the jar file
ENTRYPOINT ["java","-jar","app.jar"]
