# Dockerfile
# Use an OpenJDK base image
FROM openjdk:21
WORKDIR /app

# Copy the built JAR file to the container
COPY target/solar-watch-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]