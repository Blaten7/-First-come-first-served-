# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the build artifact (jar file) to the container
COPY build/libs/app.jar app.jar

# Expose UserService port
EXPOSE 8050

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
