## Use an official Java runtime as the base image
#FROM eclipse-temurin:21-jdk-alpine
#
## Set the working directory inside the container
#WORKDIR /app
#
## Copy the JAR file from your target folder into the container
#COPY target/support-system-0.0.1-SNAPSHOT.jar app.jar
#
## Expose the port Render will map to (your command specifies 8085)
#EXPOSE 8085
#
## Execute your exact command when the container starts
#ENTRYPOINT ["java", "-Dspring.profiles.active=supabase", "-Dserver.port=8085", "-jar", "app.jar"]

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the precompiled JAR file from your tracked target folder
COPY target/support-system-0.0.1-SNAPSHOT.jar app.jar

# Expose Render's routing port
EXPOSE 8080

# Execute the application forcing the dynamic datasource parameters ahead of the yaml
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=supabase -Dspring.datasource.url=\"$SPRING_DATASOURCE_URL\" -Dspring.datasource.username=\"$SPRING_DATASOURCE_USERNAME\" -Dserver.port=${PORT:-8080} -jar app.jar"]
