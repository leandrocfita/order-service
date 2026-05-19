# --- 1. Etapa de Build ---
# Usa uma imagem com Maven ja instalado para compilar o projeto
FROM maven:3.9.9-amazoncorretto-21-alpine as build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

# Compila o projeto e cria o JAR.
RUN mvn package -DskipTests

# --- 2. Etapa Final ---

FROM amazoncorretto:21.0.3-alpine3.19

# Expõe a porta que a aplicação Spring Boot usa
EXPOSE 8083

# 2. Cria um grupo e um usuário de sistema dedicados para rodar a aplicação
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Define o diretório de trabalho
WORKDIR /app

# 3. Copia o JAR da etapa de build e o script de inicialização
COPY --from=build /app/target/cheffy-order-service-0.0.1-SNAPSHOT.jar app.jar

# 4. Adiciona uma verificação de saúde (Health Check)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]