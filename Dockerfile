# Use official Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Compile all Java classes into bin/
RUN mkdir -p bin && javac -encoding UTF-8 -d bin -cp "lib/*" $(find src -name "*.java")

# Expose default HTTP port (Render dynamically sets $PORT)
ENV PORT=8080
EXPOSE 8080

# Start Java application in server-only headless mode (colon separator for Linux classpath)
CMD ["java", "-Djava.awt.headless=true", "-cp", "bin:lib/*", "com.hostel.mess.AppMain", "--server-only"]
