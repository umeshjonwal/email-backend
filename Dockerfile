# ===============================
# Build stage
# ===============================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# ===============================
# Runtime stage
# ===============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/*jar app.jar

# Expose port (Render will override)
EXPOSE 9090

# Run application
ENTRYPOINT ["java","-jar","app.jar"]
