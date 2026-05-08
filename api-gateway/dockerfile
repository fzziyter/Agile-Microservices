FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# On copie le JAR depuis le dossier target local vers le conteneur
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]