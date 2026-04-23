# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /app

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar y empaquetar
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copiar JAR del stage anterior
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto
EXPOSE 8080

# Comando para ejecutar
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
