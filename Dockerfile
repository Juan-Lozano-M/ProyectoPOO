# ---- Etapa 1: build ----
# Compila el proyecto con Maven y genera el .jar
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copiamos primero los archivos de Maven para aprovechar la cache de capas:
# si no cambian las dependencias, Docker reutiliza esta capa.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiamos el codigo fuente y empaquetamos (sin correr tests para acelerar el deploy)
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---- Etapa 2: runtime ----
# Imagen ligera solo con el JRE para ejecutar la aplicacion
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos unicamente el jar generado en la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Render inyecta la variable PORT; Spring Boot la lee desde application.properties
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
