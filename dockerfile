FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
# Define o diretório de trabalho dentro do container
WORKDIR /app
 
# Copia o JAR construído da fase anterior para o diretório de trabalho
COPY --from=Build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
