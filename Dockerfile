# Use an official Java runtime as the base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your target folder into the container
COPY target/support-system-0.0.1-SNAPSHOT.jar app.jar

# Expose the port Render will map to (your command specifies 8085)
EXPOSE 8085

# Execute your exact command when the container starts
ENTRYPOINT ["java", "-Dspring.profiles.active=supabase", "-Dserver.port=8085", "-jar", "app.jar"]