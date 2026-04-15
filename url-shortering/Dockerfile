# Etapa de build: gera o JAR
FROM eclipse-temurin:25-jdk AS builder
#WORKDIR /app

#COPY . .

#RUN chmod +x mvnw
#RUN ./mvnw clean package -DskipTests

WORKDIR /app
COPY pom.xml pom.xml
COPY mvnw mvnw
COPY .mvn .mvn
COPY url-shortering/pom.xml url-shortering/pom.xml
COPY url-shortering/src url-shortering/src
COPY url-shortering/lombok.config url-shortering/lombok.config
RUN ./mvnw -pl url-shortering -am clean package -DskipTests
RUN rm -rf /root/.m2/repository  # reduz camada do builder (não entra na final)

# Etapa final: usa JRE leve para rodar o JAR
FROM gcr.io/distroless/java25-debian13
WORKDIR /app

USER nonroot
# Copia apenas o JAR gerado
COPY --from=builder /app/url-shortering/target/url-shortering-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
