# Use Java 25 runtime
FROM eclipse-temurin:25-jre

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/email-writter-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 9090

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
