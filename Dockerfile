# ==========================================
# Stage 1: Build React frontend
# ==========================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# ==========================================
# Stage 2: Build Spring Boot backend with compiled frontend
# ==========================================
FROM eclipse-temurin:25-jdk AS backend-builder
WORKDIR /app

# Copy Maven wrapper and configuration
COPY backend/pom.xml backend/mvnw ./
COPY backend/.mvn ./.mvn
RUN chmod +x ./mvnw

# Pre-fetch dependencies to leverage Docker layer caching
RUN ./mvnw dependency:go-offline -B || true

# Copy backend source code
COPY backend/src ./src

# Copy compiled frontend assets into Spring Boot's static resources directory
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static/

# Package executable JAR (skip tests during image build)
RUN ./mvnw clean package -DskipTests
RUN cp target/TuristAgency-*.jar target/app.jar

# ==========================================
# Stage 3: Minimal Java 25 runtime
# ==========================================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy packaged JAR from build stage
COPY --from=backend-builder /app/target/app.jar app.jar

# Render sets the PORT env variable dynamically (usually 10000). Default to 8080 if not set.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
