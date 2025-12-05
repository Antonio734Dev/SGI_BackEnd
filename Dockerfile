# Use Eclipse Temurin 17 JDK (official OpenJDK distribution)
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (cache layer for better rebuild performance)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port (Render will inject PORT variable, default to 8080)
EXPOSE ${PORT:-8080}

# Run the application with production profile
# Render injects PORT environment variable automatically
# Using shell form to allow environment variable substitution
CMD ["sh", "-c", "java -jar -Dserver.port=${PORT:-8080} target/LabMetricas-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod"]
