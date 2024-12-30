# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the build artifact (jar file) to the container
COPY build/libs/app.jar app.jar

# Expose ProductService port
EXPOSE 8060

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
