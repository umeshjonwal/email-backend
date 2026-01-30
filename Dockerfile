# ===============================
# Build stage (with Maven)
# ===============================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# ===============================
# Runtime stage (lightweight)
# ===============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port (Render will override)
EXPOSE 9090

# Run Spring Boot app
ENTRYPOINT ["java","-jar","app.jar"]
